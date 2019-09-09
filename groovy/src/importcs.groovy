import com.opencsv.CSVReader
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBean
@GrabResolver(name='devfactory', root='https://scm.devfactory.com/nexus/content/groups/public')
@Grab('com.opencsv:opencsv:3.9')
@Grab('com.sun.jersey:jersey-bundle:1.19.3')
@Grab('org.json:json:20170516')
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@Grab('mysql:mysql-connector-java:5.1.39')
@Grab('com.devfactory.codeserver:codeserver2-client-lib:2.5.96')
@GrabConfig(systemClassLoader = false)

import com.devfactory.codeserver.client.CodeServerClient

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


class CsRepo {
    String sourceUrl
    String branch
    String language
    String revision
}

String.metaClass.encodeURL = {
    java.net.URLEncoder.encode(delegate, "UTF-8")
}

println ">>> SCRIPT STARTED <<<"

CodeServerClient codeServerClient = CodeServerClient.builder()
        .withBaseUrl(URI.create("http://codeserver.devfactory.com"))
        .build();

List<CsRepo> listOfRepos = getCsImportInput(new File("/Users/ajanoni/cgbuildsMissing.csv"));

listOfRepos.each { repo ->
    retry(5, { e -> e.printStackTrace() }) {
        println ">>>" + repo.sourceUrl
        codeServerClient.repositories().createRepository(repo.sourceUrl, repo.sourceUrl +
                "?branch=" + repo.branch, repo.language)
//
//        Optional<Repository> retRepo =codeServerClient.repositories().listBranches(URI.create(repo.sourceUrl))
//        if(retRepo.isPresent()) {
//            retRepo.get().
//            println "ok"
//        }

    }
}

List<CsRepo> getCsImportInput(File fileCsv) {
    String[] CSV_CS_MAPPING = ["sourceUrl", "branch", "language", "revision"]
    Reader fileCsvReader = new InputStreamReader(new FileInputStream(fileCsv), StandardCharsets.UTF_8)
    CSVReader csvReader = new CSVReader(fileCsvReader);
    ColumnPositionMappingStrategy<CsRepo> mappingStrategy =
            new ColumnPositionMappingStrategy<>();
    mappingStrategy.setType(CsRepo.class);
    mappingStrategy.setColumnMapping(CSV_CS_MAPPING);
    CsvToBean<CsRepo> ctb = new CsvToBean<>();
    return ctb.parse(mappingStrategy, csvReader);
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
    //throw new Exception(">>> Failed after $times retries")
}
