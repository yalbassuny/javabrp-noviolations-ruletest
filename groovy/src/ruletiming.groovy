import groovyx.net.http.ContentType
@Grab('com.opencsv:opencsv:3.9')
@Grab('com.sun.jersey:jersey-bundle:1.19.3')
@Grab('org.json:json:20170516')
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@GrabConfig(systemClassLoader = true)

import com.opencsv.CSVReader
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBean
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient

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

class Repository {
    String sourceUrl
    String branch
    String revision
}


println ">>> SCRIPT STARTED <<<"

//def cypher_query = "MATCH(if:IfStatement)-[:then| else*..]->(block:Block)-[:tree_edge]->(e) WITH if, block, collect(e) AS expressions, size((block)-[:tree_edge]->()) AS lines ORDER BY block.line WITH if, block, filter (st IN extract (ex IN expressions | CASE WHEN lines > 0 THEN CASE WHEN 'Comment' IN labels(ex) THEN 'Comment' ELSE extract (s IN (ex)-[:tree_edge*..]->() | reduce(path = '', n IN nodes(s) | CASE WHEN n.name IS NOT NULL THEN n.name WHEN n.value IS NOT NULL THEN n.value WHEN n.operator IS NOT NULL THEN n.operator ELSE null END) ) END END ) WHERE st IS NOT  NULL ) AS statements WITH if, collect(statements) AS statementsList, tail(collect(block)) AS blocks, extract(s IN statements | length(s)) AS xt, extract (s IN statements | s) AS flatStatementsList WHERE length(xt) > 0 WITH if, flatStatementsList = statementsList AS hasDifferences, blocks WHERE hasDifferences = false UNWIND blocks AS block WITH DISTINCT block, if RETURN DISTINCT block.col AS col, block.line AS line, if.file AS file ORDER BY if.file, block.line, block.col UNION ALL MATCH (switch:SwitchStatement)-[:then]->(case:SwitchSection) WITH switch, case ORDER BY case.line WITH switch, collect(case) AS switchCases WITH switch, switchCases[0..size(switchCases)] AS testedCases, switchCases MATCH (switch)-[:then]->(:SwitchSection)-[:statement]->(stmt) WHERE NOT stmt:SwitchSection WITH switch, testedCases, switchCases, stmt ORDER BY stmt.line, stmt.col WITH switch, testedCases, switchCases, collect(stmt) AS switchStatements WITH switch, REDUCE(result = [], idx IN RANGE(0, SIZE(testedCases) - 1 ) | result + { CASE:testedCases[idx], statements:filter(statement IN switchStatements WHERE statement.line >= switchCases[idx].line AND NOT 'BreakStatement' IN LABELS(statement) AND CASE WHEN switchCases[idx+1].line IS NOT NULL THEN statement.line < switchCases[idx+1].line ELSE true END ) } ) AS sortedCases UNWIND sortedCases AS case WITH switch, case.CASE AS case, case.statements AS statements, length(case.statements) AS lines WHERE lines > 0 WITH switch, filter (st IN extract (ex IN statements | CASE WHEN 'Comment' IN labels(ex) THEN null ELSE extract (s IN (ex)-[:tree_edge*..]->() | reduce(path = '', n IN nodes(s) | CASE WHEN n.name IS NOT NULL THEN n.name WHEN n.value IS NOT NULL THEN n.value WHEN n.operator IS NOT NULL THEN n.operator ELSE labels(n) END)) END ) WHERE st IS NOT  NULL ) AS statements, case WITH collect(statements) AS statementsList, tail(collect(case)) AS blocks, extract (s IN statements | s) AS flatStatementsList, switch WITH switch, flatStatementsList = statementsList AS hasDifferences, blocks WHERE hasDifferences = false UNWIND blocks AS block RETURN DISTINCT block.col AS col, block.line AS line, block.file AS file ORDER BY block.file, block.line, block.col"
def cypher_query = "MATCH (n1:Assignment)<-[:tree_edge*0..1]-()<-[:argument]-(x) WHERE NOT (n1)<-[:tree_edge*]-(:LambdaExpression) RETURN n1.col as col, n1.line as line, n1.file as file UNION MATCH (n1:Assignment)<-[:right]-(n2:Assignment) RETURN n1.col as col, n1.line as line, n1.file as file UNION MATCH (n1:Assignment)<-[:initializer]-(:VariableDeclarationFragment) RETURN n1.col as col, n1.line as line, n1.file as file UNION MATCH (id2)<-[left]-(n1:Assignment)<-[:expression]-()<-[:tree_edge*0..1]-(exp1)<-[:expression]-(), (exp1)-[:left]->(id1) WHERE NOT (n1)<-[:tree_edge*]-(:LambdaExpression) AND NOT id1.name = id2.name RETURN n1.col as col, n1.line as line, n1.file as file UNION MATCH ()<-[left]-(n1:Assignment)<-[:expression]-()<-[:tree_edge*0..1]-(:ParenthesizedExpression)<-[:expression]-() WHERE NOT (n1)<-[:tree_edge*]-(:LambdaExpression) RETURN n1.col as col, n1.line as line, n1.file as file"

def repos = []

List<Repository> listRepos = loadFromFile('/Users/ajanoni/timing_repos_1.csv')

listRepos.each {item ->
    repos.add(
    [
            repo_url: item.sourceUrl,
            branch: item.branch,
            commit: item.revision
    ])
}

//def cgClientPOST = new RESTClient('http://brp-exp.ecs.devfactory.com/load-test/cg2')
def cgClientPOST = new RESTClient('http://10.224.97.197:46796/load-test/cg2')
cgClientPOST.getClient().params.setParameter("http.connection.timeout", 1200000)
cgClientPOST.getClient().params.setParameter("http.socket.timeout", 1200000)

System.out.println(new Date().toString())
cgClientPOST.request(Method.POST, ContentType.JSON) { req2 ->
    body = [cypher_query    : cypher_query,
            repos    : repos
    ]

    response.success = { resp2, json2 ->
        json2.violations_with_timings.each {i ->
            println "${i?.repo_url},${i?.timings?.BEFORE_ACTIVATION},${i?.timings?.AFTER_AUTHENTICATION}," +
                    "${i?.timings?.BEFORE_QUERY_EXECUTED},${i?.timings?.AFTER_QUERY_EXECUTED}"
        }
    }

    response.failure = { resp2 ->
        println "Unexpected error: ${resp2.statusLine.statusCode}"
        println "${resp2.entity.content.text}"
    }
}
System.out.println(new Date().toString())


List<Repository> loadFromFile(fileName) {
    String[] CSV_REPORT_MAPPING = ["sourceUrl", "branch", "revision"]
    File fileCsv = new File(fileName)
    Reader fileCsvReader = new InputStreamReader(new FileInputStream(fileCsv), StandardCharsets.UTF_8)
    CSVReader csvReader = new CSVReader(fileCsvReader, (char)',');
    ColumnPositionMappingStrategy<Repository> mappingStrategy =
            new ColumnPositionMappingStrategy<>();
    mappingStrategy.setType(Repository.class);
    mappingStrategy.setColumnMapping(CSV_REPORT_MAPPING);
    CsvToBean<Repository> ctb = new CsvToBean<>();
    return ctb.parse(mappingStrategy, csvReader);
}
