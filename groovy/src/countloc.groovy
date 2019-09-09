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


class CgLine {
    String sourceUrl
    String branch
    String language
    String revision
}

String.metaClass.encodeURL = {
    java.net.URLEncoder.encode(delegate, "UTF-8")
}

println ">>> SCRIPT STARTED LOC<<<"

ICodegraphClient cgClient = new CodegraphClient(
        URI.create("http://codegraph-rest-server-prod.ecs.devfactory.com/api/v1.0/"), "brp_th", "brp_th");

List<CgLine> listOfRepos = getCgImportInput(new File("/Users/ajanoni/Downloads/loc_script_10.tsv"));

List<String> messages = new ArrayList<>();

List<Build> listBuild = cgClient.allBuilds.sort{it.createdAt};
GParsPool.withPool(30) {
    listOfRepos.eachParallel { repo ->
        Build buildExist = listBuild.find { x ->
            x.getRequest().getSourceLocation().toString().equalsIgnoreCase(repo.getSourceUrl()) &&
                    x.getRequest().getBranch().equalsIgnoreCase(repo.getBranch()) &&
                    x.getRequest().getCommit().equalsIgnoreCase(repo.getRevision()) &&
                    x.getRequest().getLanguage().toString().equalsIgnoreCase(repo.getLanguage()) &&
                    x.getStatus().equalsIgnoreCase("successful") &&
                    x.createdAt != null
        }

        if (buildExist!=null) {
            Optional<ISandbox> sandbox = getSandBox(buildExist, cgClient);
            if(!sandbox.isPresent()) {
                messages.add(repo.getSourceUrl() + "," + repo.getBranch() + "," + repo.getRevision() + "," + repo.getLanguage() + ", NO SANDBOX");
            } else {
                try {
                    result = new JsonSlurper().parseText(sandbox.get().executeQuery("MATCH (f:File) RETURN sum(toInteger(f.CountLine)) as loc", QueryType.CYPHER));
                    def loc = (Long) result.results[0].data[0].row[0];
                    messages.add(repo.getSourceUrl() + "," + repo.getBranch() + "," + repo.getRevision() + "," + repo.getLanguage() + "," + loc);
                } catch(e) {
                    messages.add(repo.getSourceUrl() + "," + repo.getBranch() + "," + repo.getRevision() + "," + repo.getLanguage() + ",ERROR GETTING RESULTS");
                }

//                try {
//                    cgClient.deactivateSandbox(buildExist.getRequestId());
//                } catch(ex){
//                    println "Erro deactivating sandbox:" + ex.getMessage()
//                }
            }
        } else {
            messages.add(repo.getSourceUrl() + "," + repo.getBranch() + "," + repo.getRevision() + "," + repo.getLanguage() + ",BUILD DOES NOT EXIST");
        }
    }

    def resultFile = new File("/Users/ajanoni/Downloads/loc_script_10.log.tsv")
    resultFile.withWriter{ out ->
        messages.each {out.println it}
    }
}

Optional<ISandbox> getSandBox(Build build, ICodegraphClient cgClient) {
    int nodes;
    def result;
    try {
        ISandbox sandbox;
        retry{
            sandbox = cgClient.getSandbox(build.getRequestId());
            if (!sandbox.getStatus().equals(SandboxStatus.ACTIVE)) {
                cgClient.activateSandbox(build.getRequestId());
            }
            result = new JsonSlurper().parseText(sandbox.executeQuery("MATCH (n) RETURN count(n) as col, 0 as line, 'files' as file", QueryType.CYPHER));
            nodes = (Integer) result.results[0].data[0].row[0];
        }
        if(nodes>1) {
            return Optional.of(sandbox);
        } else {
            println "Empty neo4j db:" + build.getRequestId();
        }
    } catch (CodegraphClientException | IOException e) {
        println "Error from CodeGraph: " + e.getMessage();
    } catch (ex) {
        println result;
        println ex.getMessage()
    }
    return Optional.empty();
}


List<CgLine> getCgImportInput(File fileCsv) {
    String[] CSV_CS_MAPPING = ["sourceUrl", "branch", "revision", "language"]
    Reader fileCsvReader = new InputStreamReader(new FileInputStream(fileCsv), StandardCharsets.UTF_8)
    CSVReader csvReader = new CSVReader(fileCsvReader, (char)'\t');
    ColumnPositionMappingStrategy<CgLine> mappingStrategy =
            new ColumnPositionMappingStrategy<>();
    mappingStrategy.setType(CgLine.class);
    mappingStrategy.setColumnMapping(CSV_CS_MAPPING);
    CsvToBean<CsCg> ctb = new CsvToBean<>();
    return ctb.parse(mappingStrategy, csvReader);
}

def retry(int times = 5, Closure errorHandler = { e -> println e.message }
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
