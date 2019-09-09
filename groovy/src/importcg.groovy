import com.devfactory.codegraph.client.core.ICodegraphClient
import com.devfactory.codegraph.client.core.ISandbox
import com.devfactory.codegraph.client.core.impl.CodegraphClient
import com.devfactory.codegraph.client.exceptions.CodegraphClientException
import com.devfactory.codegraph.client.models.Build
import com.devfactory.codegraph.client.models.Language
import com.devfactory.codegraph.client.models.QueryType
import com.devfactory.codegraph.client.models.Request
import com.devfactory.codegraph.client.models.SandboxStatus
import com.google.common.annotations.VisibleForTesting
import com.opencsv.CSVReader
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBean
import groovy.json.JsonSlurper
import groovyx.gpars.GParsPool
import org.codehaus.jackson.map.ObjectMapper

import java.nio.charset.StandardCharsets

@GrabResolver(name = 'devfactory', root = 'https://scm.devfactory.com/nexus/content/groups/public')
@GrabResolver(name = 'neo4j', root = 'http://m2.neo4j.org/content/groups/everything')
@Grab('com.opencsv:opencsv:3.9')
@Grab('com.sun.jersey:jersey-bundle:1.19.3')
@Grab('org.json:json:20170516')
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@Grab('mysql:mysql-connector-java:5.1.39')
@Grab('com.devfactory.codegraph:client:1.0.2')
@GrabConfig(systemClassLoader = false)

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


class CsCg {
    String sourceUrl
    String branch
    String language
    String revision
}

String.metaClass.encodeURL = {
    java.net.URLEncoder.encode(delegate, "UTF-8")
}

println ">>> SCRIPT STARTED <<<"

ICodegraphClient cgClient = new CodegraphClient(
        URI.create("http://codegraph-rest-server-prod.ecs.devfactory.com/api/v1.0/"), "brp_th", "brp_th");

List<CsCg> listOfRepos = getCgImportInput(new File("/Users/ajanoni/my_part.csv"));

List<String> messages = new ArrayList<>();

List<Build> listBuild = cgClient.allBuilds;
GParsPool.withPool(10) {
    listOfRepos.eachParallel { repo ->
        Build buildExist = listBuild.find { x ->
            x.getRequest().getSourceLocation().toString().equalsIgnoreCase(repo.getSourceUrl()) &&
                    x.getRequest().getBranch().equalsIgnoreCase(repo.getBranch()) &&
                    x.getRequest().getCommit().equalsIgnoreCase(repo.getRevision()) &&
                    x.getRequest().getLanguage().toString().equalsIgnoreCase(repo.getLanguage()) &&
                    x.getStatus().equalsIgnoreCase("successful")
        }



        if (buildExist!=null) {
            if(isGoodBuild(buildExist, cgClient)) {
                messages.add(repo.getSourceUrl() + "," + repo.getBranch() + "," + repo.getRevision() + "," + repo.getLanguage() + ", OK");
            } else {
                messages.add(repo.getSourceUrl() + "," + repo.getBranch() + "," + repo.getRevision() + "," + repo.getLanguage() + ", NOK BUILD");
            }
        } else {
            println repo.getSourceUrl() + "," + repo.branch + "," + repo.revision + "," + repo.language + "," + "NOK"
//            Request req = Request.builder()
//                    .sourceLocation(URI.create(repo.sourceUrl))
//                    .branch(repo.branch)
//                    .commit(repo.revision)
//                    .language(Language.fromValue(repo.language))
//                    .build();
//
//            retry(1, { e -> messages.add(repo.getSourceUrl() + "," + repo.getBranch() + "," + repo.getRevision() + "," + repo.getLanguage() + "," + e.getMessage()) }) {
//                cgClient.triggerBuild(req);
//            }

        }
    }


    def resultFile = new File("/Users/ajanoni/my_part_result.csv")
    resultFile.withWriter{ out ->
        messages.each {out.println it}
    }
    

}

@VisibleForTesting
boolean isGoodBuild(Build build, ICodegraphClient cgClient) {
    int nodes;
    def result;
    try {
        ISandbox sandbox = cgClient.getSandbox(build.getRequestId());
        if (!sandbox.getStatus().equals(SandboxStatus.ACTIVE)) {
            cgClient.activateSandbox(build.getRequestId());
        }
        result = new JsonSlurper().parseText(sandbox.executeQuery("MATCH (n) RETURN count(n) as col, 0 as line, 'files' as file", QueryType.CYPHER));
        nodes = (Integer) result.results[0].data[0].row[0];
        cgClient.deactivateSandbox(build.getRequestId());
    } catch (CodegraphClientException | IOException e) {
        log.error("Error from CodeGraph: {}", e.getMessage());
        return false;
    } catch (ex) {
        println result;
        println ex.getMessage()
    }
    return nodes > 1;
}


List<CsCg> getCgImportInput(File fileCsv) {
    String[] CSV_CS_MAPPING = ["sourceUrl", "branch", "revision", "language"]
    Reader fileCsvReader = new InputStreamReader(new FileInputStream(fileCsv), StandardCharsets.UTF_8)
    CSVReader csvReader = new CSVReader(fileCsvReader);
    ColumnPositionMappingStrategy<CsRepo> mappingStrategy =
            new ColumnPositionMappingStrategy<>();
    mappingStrategy.setType(CsRepo.class);
    mappingStrategy.setColumnMapping(CSV_CS_MAPPING);
    CsvToBean<CsCg> ctb = new CsvToBean<>();
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
