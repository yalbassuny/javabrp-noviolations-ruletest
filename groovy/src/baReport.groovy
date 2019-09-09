import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBean
@Grab('com.opencsv:opencsv:3.9')
@Grab('com.sun.jersey:jersey-bundle:1.19.3')
@Grab('org.json:json:20170516')
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@Grab('mysql:mysql-connector-java:5.1.39')
@GrabConfig(systemClassLoader = true)

import groovy.sql.Sql
import groovy.transform.EqualsAndHashCode
import org.apache.commons.collections.CollectionUtils

import java.nio.charset.StandardCharsets

/**
 * Created by ajanoni on 08/06/17.
 *
 * ####################################################################
 * !!!!! IMPORTANT !!!!!
 * If you are running it from IntelliJ:
 * Alt+Enter with a caret positioned on @Grab to download artifacts.
 * ####################################################################
 *
 */

@EqualsAndHashCode(excludes = ['sourceUrl', 'branch', 'revision'])
class ViolationItem {
    String sourceUrl
    String branch
    String revision
    String issue
    String file
    Integer line
}

class ReportInput {
    String sourceUrl
    String branch
    String startRevision
    String endRevision
}

main();

void main() {

    List<String[]> resultList = new ArrayList<>();



    input = getReportInput()
    input.each { item ->
        //retItemA = getViolations('https://scm-ba.devfactory.com/scm/cap2/services-cim-ccm.git/?branch=Master_15April2017', 'Master_15April2017', 'b0ba00be9ea7a580d615b8b9f3b3779a4be35f3d')
        String startBranch = item.branch + "_15April2017"
        String endBranch = item.branch + "_15July2017"

        def startRevision
        def endRevision
        def totalIntersect = "N/A"
        def onlyStart = "N/A"
        def onlyEnd = "N/A"

        totalStart = "N/A"
        totalEnd = "N/A"

        messageStart = getBrpMessage(item.sourceUrl, startBranch, item.startRevision)

        if (messageStart.contains("Processed successfully.")) {
            startRevision = getViolations(item.sourceUrl, startBranch, item.startRevision)
            totalStart = startRevision.size()
        } else {
            totalStart = messageStart
        }

        messageEnd = getBrpMessage(item.sourceUrl, endBranch, item.endRevision)

        if (messageEnd.contains("Processed successfully.")) {
            endRevision = getViolations(item.sourceUrl, endBranch, item.endRevision)
            totalEnd = endRevision.size()
        } else {
            totalEnd = messageEnd
        }


        if (startRevision!=null && endRevision!=null) {
            totalIntersect = startRevision.intersect(endRevision).size()
            onlyStart = (startRevision - endRevision).size()
            onlyEnd = (endRevision - startRevision).size()
        }

        println item.sourceUrl + ":" + item.branch + ":" + totalStart + ":" + totalEnd + ":" + totalIntersect + ":" + onlyStart + ":" + onlyEnd

        resultList.add([item.sourceUrl, item.branch, totalStart, totalEnd, totalIntersect, onlyStart, onlyEnd])
    }
    writeCSV(resultList)
}


void writeCSV(List<String[]> list) {
    resultPath = "/Users/ajanoni/sonarcsv/brp_report_result_3.csv"
    Writer fileRbfWriter = new OutputStreamWriter(
            new FileOutputStream(resultPath),
            StandardCharsets.UTF_8)
    CSVWriter reportWriter = new CSVWriter(fileRbfWriter, (char) ",")

    for (String[] item : list) {
        String[] reportLine = new String[7];
        reportLine[0] = item[0]; // sourceUrl
        reportLine[1] = item[1]; // branch
        reportLine[2] = item[2]; //totalStart
        reportLine[3] = item[3]; //totalEnd
        reportLine[4] = item[4]; //totalIntersect
        reportLine[5] = item[5]; //onlyStart
        reportLine[6] = item[6]; //onlyEnd

        reportWriter.writeNext(reportLine);
    }

    reportWriter.flush()
    reportWriter.close()

}

List<ReportInput> getReportInput() {
    String[] CSV_REPORT_MAPPING = ["sourceUrl", "branch", "startRevision", "endRevision"]
    File fileCsv = new File("/Users/ajanoni/sonarcsv/brp_data_report.csv")
    Reader fileCsvReader = new InputStreamReader(new FileInputStream(fileCsv), StandardCharsets.UTF_8)
    CSVReader csvReader = new CSVReader(fileCsvReader);
    ColumnPositionMappingStrategy<ReportInput> mappingStrategy =
            new ColumnPositionMappingStrategy<>();
    mappingStrategy.setType(ReportInput.class);
    mappingStrategy.setColumnMapping(CSV_REPORT_MAPPING);
    CsvToBean<ReportInput> ctb = new CsvToBean<>();
    return ctb.parse(mappingStrategy, csvReader);
}

String getBrpMessage(String sourceUrl, String branch, String revision) {
    def dbUrl = 'jdbc:mysql://devfactory-aurora-1.cluster-cd1ianm7fpxp.us-east-1.rds.amazonaws.com';
    def dbPort = 3306
    def dbSchema = 'prod_brp'
    def dbUser = 'prod_brp'
    def dbPassword = 'dataprod123'
    def dbDriver = 'com.mysql.jdbc.Driver'
    def message = "N/A"

    Sql.withInstance(dbUrl + ':' + dbPort + '/' + dbSchema, dbUser, dbPassword, dbDriver) { sql ->
        def check = "SELECT message AS message FROM brp_data b " +
                "WHERE b.source_url LIKE CONCAT(:REPO_URL, '%') " +
                "AND b.branch = :BRANCH " +
                "AND b.revision = :REVISION "
        List<Map> brpDataList = sql.rows(check, REPO_URL: sourceUrl, BRANCH: branch, REVISION: revision)

        if (!CollectionUtils.isEmpty(brpDataList)) {
            message = brpDataList.get(0).get("message")
        }
    }

    return message

}

Set<ViolationItem> getViolations(String sourceUrl, String branch, String revision) {

    Set<ViolationItem> retItems = new HashSet<ViolationItem>();
    def dbUrl = 'jdbc:mysql://devfactory-aurora-1.cluster-cd1ianm7fpxp.us-east-1.rds.amazonaws.com';
    def dbPort = 3306
    def dbSchema = 'prod_brp'
    def dbUser = 'prod_brp'
    def dbPassword = 'dataprod123'
    def dbDriver = 'com.mysql.jdbc.Driver'

    Sql.withInstance(dbUrl + ':' + dbPort + '/' + dbSchema, dbUser, dbPassword, dbDriver) { sql ->

        def getViolationsStart = "SELECT v.issue_key, v.file_key, v.line_number, count(1) AS violations " +
                "FROM violation_data v " +
                "INNER join brp_data b on (b.id = v.brp_data_id) " +
                "WHERE b.source_url LIKE CONCAT(:REPO_URL, '%') " +
                "AND b.branch = :BRANCH " +
                "AND b.revision = :REVISION " +
                //"AND b.message LIKE 'Processed successfully.%' " +
                "GROUP BY v.issue_key, v.file_key, v.line_number "


        sql.eachRow(getViolationsStart, REPO_URL: sourceUrl, BRANCH: branch, REVISION: revision) {
            row ->
                retItems.add(
                        new ViolationItem(
                                sourceUrl: sourceUrl,
                                branch: branch,
                                revision: revision,
                                issue: row[0],
                                file: row[1],
                                line: row[2]))
        }


    }
    return retItems
}


def retry(int times = 5, Closure errorHandler = { e -> log.warn(e.message, e) }
          , Closure body) {
    int retries = 0
    def exceptions = []
    while (retries++ < times) {
        try {
            if (retries > 1) {
                println ">>> Attempt:" + retries
            }
            return body.call()
        } catch (e) {
            exceptions << e
            errorHandler.call(e)
        }
    }
    throw new Exception(">>> Failed after $times retries")
}
