/**
 * Created by ajanoni on 31/05/17.
 *
 *
 * ####################################################################
 * !!!!! IMPORTANT !!!!!
 * If you are running it from IntelliJ:
 * Alt+Enter with a caret positioned on @Grab to download artifacts.
 * ####################################################################
 *
 */


@Grab('com.opencsv:opencsv:3.9')
@Grab('com.sun.jersey:jersey-bundle:1.19.3')
@Grab('org.json:json:20170516')
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')

import com.opencsv.CSVWriter
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.WebResource
import com.sun.jersey.api.client.config.ClientConfig
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.json.JSONConfiguration
import groovy.transform.EqualsAndHashCode
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.nio.charset.StandardCharsets

@EqualsAndHashCode(excludes = ['col','file'])
class Violation {
    String projectName
    String file
    String fileName
    Integer line
    Integer col
}

String.metaClass.encodeURL = {
    java.net.URLEncoder.encode(delegate, "UTF-8")
}

cgErrorList = new ArrayList<String[]>()

//queryS2444 = "MATCH (nField:FieldDeclaration)-[:tree_edge]->(nVar:VariableDeclarationFragment)<-[:SETS]-(nMethod:MethodDeclaration) MATCH (nMethod)-[:tree_edge*]->(var)<-[:SET_BY]-(nVar) WHERE nField.modifiers CONTAINS 'static' AND NOT nField.modifiers CONTAINS 'volatile' AND NOT nField.modifiers CONTAINS 'final' AND NOT nMethod.modifiers CONTAINS 'synchronized' AND NOT (var)<-[:tree_edge*]-(:SynchronizedStatement) AND NOT (nField)-[:tree_edge*]->(:PrimitiveType) RETURN DISTINCT var.col AS col, var.line AS line, var.file AS file ORDER BY var.file, var.line";

// queryS1143 = "MATCH (n:ReturnStatement)<-[:tree_edge*]-(:Block)<-[:finally]-(:TryStatement) " +
//        "RETURN DISTINCT n.col AS col, n.line AS line, n.file AS file " +
//        "UNION " +
//        "MATCH (n:BreakStatement)<-[:tree_edge*]-(:Block)<-[:finally]-(:TryStatement) " +
//        "RETURN DISTINCT n.col AS col, n.line AS line, n.file AS file " +
//        "UNION " +
//        "MATCH (n:ContinueStatement)<-[:tree_edge*]-(:Block)<-[:finally]-(:TryStatement) " +
//        "RETURN DISTINCT n.col AS col, n.line AS line, n.file AS file " +
//        "UNION " +
//        "MATCH (n:ThrowStatement)<-[:tree_edge*]-(:Block)<-[:finally]-(:TryStatement) " +
//        "RETURN DISTINCT n.col AS col, n.line AS line, n.file AS file"

//queryS1444 = "MATCH (c)-[:tree_edge*]->(field:FieldDeclaration) " +
//        "WHERE field.modifiers CONTAINS ('static') " +
//        "AND field.modifiers CONTAINS ('public') " +
//        "AND NOT field.modifiers CONTAINS ('final') " +
//        "AND NOT c.entity_type = 'interface' " +
//        "WITH COLLECT(DISTINCT(field)) AS All " +
//        "OPTIONAL MATCH (field)<-[:member]-(:TypeDeclaration)-[:annotation]->(sma:SingleMemberAnnotation) " +
//        "WHERE sma.name = 'StaticMetamodel' " +
//        "WITH COLLECT(DISTINCT(field)) AS SmaClasses, All " +
//        "WITH FILTER(x IN All WHERE NOT x IN SmaClasses) AS FilteredResult " +
//        "UNWIND FilteredResult AS FinalResult " +
//        "MATCH(FinalResult)-[:fragment]->(vd:VariableDeclarationFragment) " +
//        "RETURN vd.col AS col, vd.line AS line, vd.file AS file"


//queryDM_NUMBER_CTOR = "MATCH (newInstance:ClassInstanceCreation)-[:type]->(type:SimpleType)\n" +
//        "WHERE type.name in ['Long','Integer', 'Short', 'Character', 'Byte']\n" +
//        "AND NOT newInstance.file CONTAINS ('src/test') //Do not get violations in test files\n" +
//        "RETURN DISTINCT newInstance.col as col, newInstance.line as line, newInstance.file as file\n" +
//        "ORDER BY newInstance.file,newInstance.line\n"
//
//
//queryS1116 = "MATCH (n:EmptyStatement)\n" +
//        "RETURN DISTINCT n.col as col, n.line as line, n.file as file"


//queryS1700 = "MATCH (n:TypeDeclaration)-[:name]->(className:SimpleName), \n" +
//        "(n)-[:tree_edge]->(field:FieldDeclaration)-[:fragment]->(:VariableDeclarationFragment)-[:name]->(fieldName:SimpleName),\n" +
//        "(field)-[:type*]->()-[:name*0..1]->(fieldType)\n" +
//        "WHERE LOWER(className.name) = LOWER(fieldName.name)\n" +
//        "AND NOT (LOWER(fieldType.name) = LOWER(className.name) AND field.modifiers CONTAINS 'static')\n" +
//        "RETURN DISTINCT field.col as col, field.endLine as line, field.file as file\n" +
//        "UNION\n" +
//        "MATCH (enum:EnumDeclaration)-[:tree_edge]->(field:FieldDeclaration)-[:fragment]->(:VariableDeclarationFragment)-[:name]->(fieldName:SimpleName),\n" +
//        "(field)-[:tree_edge]-(fieldType)\n" +
//        "WHERE LOWER(enum.name) = LOWER(fieldName.name)\n" +
//        "AND NOT (LOWER(fieldType.name) ENDS WITH LOWER(enum.name) AND field.modifiers CONTAINS 'static')\n" +
//        "RETURN DISTINCT field.col as col, field.endLine as line, field.file as file"

queryS2737 = "MATCH (tryStm:TryStatement)-[:catch]->(n:CatchClause)-[:body]->(block:Block)-[:statement]->(tr:ThrowStatement)-[:throws]->(varName1),\n" +
        "(n)-[:declaration]->()-[:name]->(varName2)\n" +
        "WHERE size((block)-[:statement]->())=1\n" +
        "AND size((tryStm)-[:catch]-(:CatchClause))=1\n" +
        "AND varName1.name = varName2.name \n" +
        "RETURN DISTINCT tr.col as col, tr.line as line, tr.file as file"

//cgProjects = [
//        'business-payment'                        : 'a98c9c54-23c1-4dfc-a9d1-5335c1368af3',
//        'versata-m1.ems'                          : 'e494eb5f-5a45-4259-b2a7-714f16dbd6b1',
//        'aurea-sonic-mq'                          : '5beea8ba-c579-46f8-8c9e-c94124a4f12e',
//        'ignite-sensage-analyzer'                 : '28ade68a-dd2e-405e-a87a-549ecc0cf57d',
//        'ta-smartleads-lms-mct'                   : '223915ff-d7cc-4acc-9b23-6c238c77f39a',
//        'aurea-aes-edi'                           : '7fc8e251-9fca-4861-b245-8ccde4580f67',
//        'pss'                                     : 'aad7ba6b-8069-4aa3-8a36-38cc5c5e20d1',
//        'kerio-mykerio-kmanager'                  : 'cb7cd6df-a193-444f-8a17-633da0025a18',
//        'aurea-lyris-platform-edge'               : '34267f92-0883-4004-bd33-1c570ab54552',
//        'devfactory-codegraph-server'             : '0db9b092-2e84-438e-949e-5abaad903dad',
//        'aurea-java-brp-cs-ruletest'              : '9a3f2b9b-3bfa-4b99-b363-f1d0659fa778',
//        'org.jenkins-ci.main:pom'                 : '15e366aa-fc18-4ea0-bec5-9443a2a7a8f4',
//        'hibernate-orm'                           : 'f65e663c-ab46-44e2-be13-03d84eda1bf3',
//        'org.apache.wicket:wicket-parent'         : '37821c2d-f736-46bc-b1a4-fa2400dab0e3',
//        'org.apache.struts:struts2-parent'        : 'f31fed00-936f-47ae-8f83-71e6e223b8a8',
//        'org.springframework:spring'              : 'ce1ab499-2bc4-49c8-adf9-8bf1f4e2e1e7',
//        'org.apache.hive:hive'                    : '68151e65-78ab-46f8-aa28-7e90ef6168f8',
//        'org.drools:drools'                       : 'b6546cab-7dd2-4748-99af-738658c7b5d7',
//        'org.apache.activemq:activemq-parent'     : 'ea4b9d5a-35d3-4810-b605-7f7983062ff9',
//        'org.eclipse.jetty:jetty-project'         : '1def6b67-e748-43e4-90a8-d9627e6688c6',
//        'org.eclipse.jgit:org.eclipse.jgit-parent': '43c578b3-bd45-4fee-8ba4-1acfdb449ab8'
//]



cgProjects = [
        'maven':'7a0fa3bb-eb05-474b-a3c8-a7a539d2cc63',
        'bitcoinj':'95027d5a-167e-4afb-b70f-3aaea6f1db62',
        'grunt-contrib-jshint':'df70dc57-7d02-4c9d-a24a-532ffac0dc4c',
        'jasmine':'459a3cd4-ed11-44fa-85d8-b64bd6921d62',
        'MiniBlog':'4953b20f-015b-4142-be45-b0db16e07214',
        'LiteDB':'e1946089-f4a4-493e-a0f2-bd3c61fbb96a',
        'protobuf-net':'4e18183e-5a0c-49cf-a9e3-9c3077800235',
        'Nancy':'dd938c1e-2af8-4644-bd3d-5d4ea7bd0006',
        'nhibernate-core':'0218d32c-04ec-48c4-b617-2899184a6880',
        'nopCommerce':'602c4fb8-ae3c-407b-bc7a-1a3c2fa63528',
        'ModernHttpClient':'00a08e3f-b6e9-451c-8088-fcc7c2023604',
        'Leaflet':'bfa31277-2409-46b7-8efb-044f7ffc7d14',
        'cs_sample':'381b67d1-90a4-4e52-864a-cf0a6abbafb7',
        'rest-api-violation':'145882e5-4428-42aa-b312-1c963b98ab3d',
        'RestSharp':'93d1eb5c-7d42-4320-a7e3-355fa65bdcca',
        'fluentmigrator':'cc989f1f-2b5a-4652-b615-50fa120e2f93',
        'SignalR':'45e4ef20-2835-4afe-aaac-12abdee35b8b',
        'aws-maven':'829211b3-09e1-4c0d-9e1a-d6693aa78706',
        'okhttp':'0ec44e77-dd4f-4f06-a66f-1c9f2d39d977',
        'aLine-FirewalForCode':'b2733b68-8e95-4b37-9f3a-77a8232e5c31',
        'aurea-ace-generix':'accbc5b0-b470-403d-894f-9a27c6fbdd1e',
        'aurea-crm-aline':'6ea4706d-83d0-400a-8c61-5c922ca6ca47',
        'aurea-crm-office-addin':'d79b8c62-ccbe-4605-8c0a-980c3379aeba',
        'aurea-dxsi':'66d33df4-a0a6-408e-b986-c18bf3324ce6',
        'aurea-ipm-main':'e9835ef3-f79b-4fea-9ee6-eb0762f48b66',
        'aurea-nextdocs-adlib':'be1b88d7-aad1-468b-8565-b48d6fd4952a',
        'aurea-sonic-mq':'e882df5c-0aff-4ac8-bd7b-93715dcd4464',
        'bc-java':'b9e80244-3f75-4d37-a815-3db32ee75914',
        'devfactory-docker-scorecard':'0a80a437-8ff8-468e-8471-5b7340bb2937',
        'devfactory-ideplugins2.0-visualstudio-plugin':'86f7e6ee-ef1f-4edc-a7e5-28b79ed534a9',
        'gfi-eventsmanager':'2e39d655-aff0-42d0-a374-dc0d4ceb26d0',
        'gfi-oneguard':'27f12203-7002-4a9e-8e43-da044938c4ef',
        'kerio-connect-connect':'f1d4541e-01fc-4b46-9af9-94a22287a398',
        'kerio-winroute-at-mykerio':'548bf803-621f-4f96-a18f-ed13893bdf83',
        'QuickSearch-demo':'e06f314a-92b6-443a-8888-80aa435e787d',
        'versata-epmlive-epml-c2':'7e45e24a-28b0-4e23-996c-f91a28ec6694',
        'versata-m1.client-ems-client':'196fcc5b-6665-4c2b-8965-46632c0b856e',
        'business-avios':'bdfc1e85-699c-46b8-94fc-87e1413752d7',
        'business-framework':'5edb4d25-bbe1-4445-93c8-812f14b53c83',
        'business-redemptions-builders':'587ab054-2d61-43b3-aa9c-ce13121e729c',
        'reservations-common':'1b39a13f-4c77-441a-8774-2990b0daf6cf',
        'web-contextualisation':'f11a9c50-2091-49cd-8586-6de7a03aa71d',
        'web-diagtool':'cf53c631-ca63-4513-94fe-85e805b88866',
        'web-framework-schema':'4cc2ae1f-175b-4cce-8831-3eb0f3e17470',
        'web-payment':'c23fca66-7921-4186-abfc-44c61bb3b567',
        'web-selling-builders':'24c17a1d-8422-4d8a-96e1-8bb6e5bbaa29',
        'web-sitenavigation':'1471cfbe-2569-4496-b49e-b89d5b914be3',
        'web-xmlhttp-proxy':'e2d419da-f0e9-4489-ac75-27e1efb22505',
        'fc-baflt-bafma':'d8897271-183f-4de6-8f83-7974d3e74fac',
        'fc-ndc-cma':'905faf9a-0cd4-475a-9606-d62e3872a9c9',
        'fc-pym-vpa':'c890ccf1-5c17-4788-ab77-26cc4e71bfa8',
        'fc-sea-sesa':'cbc5b8ea-de3b-4f80-bfed-94dc535bac1b',
        'mp-evm-evtm':'cb1d7f73-0a8d-481f-9e0f-6feeabae7797',
        'aaui-cpm':'c9b8609e-83ef-47fd-a451-62b207e14fab',
        'acd-emdm':'e62e1d80-4d33-4bde-92ca-94e02743309c',
        'asm-ema':'50f13e6d-f104-449d-b884-8a0014b17441',
        'fc-baflt-bafma':'fa4d9ca6-67e9-4ae0-b5eb-4caf1e10571e',
        'services-bds-mdp':'778527ed-c225-4efa-8e2e-2d485041fd6f',
        'services-car-cav':'7bbcaa54-a9d4-4641-8e99-deb33888e1b2',
        'services-cargopub-cargopub':'696d5e5b-fcd6-4b42-9715-b376c1cc1c26',
        'services-cem-cpr':'ac055ec4-ae3c-4045-a056-430dbc14c0ac',
        'services-cmg-ccb':'4fac24b4-fe5b-4b8a-97ac-8484ea04b008',
        'services-concur-tem':'2ab2f23a-200f-4960-bb1e-44e368b74af9',
        'services-core-ehcache':'2b3c1569-519e-4aa2-93be-8c64f36046c2',
        'services-crw-ccbt':'98c7d330-a4a4-451a-adaf-52d668f1acad',
        'services-dvm-tcm':'f11ed2cd-655d-4e76-a5e8-e60e3dc94d18',
        'services-fli-cbu':'d8119309-34de-4a32-932a-6a4d310cfd13',
        'services-fom-foma':'ba9cdc80-c7c2-4e0b-9d3f-4fcd019c3957',
        'services-iata-ssba':'af97746f-722c-4f7d-8816-1ec9e496e4f1',
        'services-meo-lca':'e22fb4f9-c16b-43d6-a7db-bb00a31e1253',
        'services-ndc-dist':'4caa2a86-002f-4436-954c-0aca4c7477fa',
        'services-orm-ordrt':'bef09daf-a0c6-4893-8286-6ac5ee7e51c4',
        'services-pega-cma':'78671aad-6349-4c54-a85b-665be44fd63f',
        'services-sas-mfas':'f8f23e15-5a63-4bb2-9a9c-450b047ebb55',
        'services-sse-invm':'fae43d1c-b3fe-4ed0-8783-7d444d349e06',
        'services-sse-sbkm':'5e274a6d-98c1-4a10-b2e9-117dc356b399',
        'services-svrm-manc':'19962ade-aeac-4bc6-8d71-d9a0f38f8611',
        'bandcamp':'edac44f8-e66b-4436-b911-11ee488bcff7',
        'gfi-oneconnect-M1-EMSClient':'897b114d-b88a-423a-8666-28de1237ffee',
        'eclipse':'25ba5d28-3475-48b6-a5ba-b4d9722e3be5',
        'leadlander':'25ba5d28-3475-48b6-a5ba-b4d9722e3be5'
]



fileLocal = "/Users/ajanoni/sonarcsv"

//sonarBaseUrl = "http://netbrp-sonarqube-stg.ecs.devfactory.com/"
sonarBaseUrl = "http://brp-sonar.ecs.devfactory.com"

//ruleId = "rules=csharpsquid%3AS1116"
ruleId = "rules=squid%3AS2737" //CHANGE THE RULE NAME HERE
//ruleId = "rules=findbugs%3ADM_NUMBER_CTOR"

ruleName = "S2737"

sonarViolation = toViolationDTO(findAllSonarViolations())

cgViolation = getViolationFromCG(queryS2737) //CHANGE THE QUERY HERE

//exportToCsvSummary(ruleName, sonarViolation, cgViolation) //CHANGE THE QUERY HERE

//saveCGErrors(ruleName)

exportToCsvDiff(ruleName, sonarViolation, cgViolation)

List<Violation> getViolationFromCG(String query) {

    List<Violation> retViolation = new ArrayList<>()

    cgProjects.each { k, v ->
        def cgClient = new RESTClient('https://codegraph-api-prod.ecs.devfactory.com/api/1.0/graphs/' + v + '/query')
        cgClient.getClient().params.setParameter("http.connection.timeout", 30000)
        cgClient.getClient().params.setParameter("http.socket.timeout", 30000)
        println k + ' - https://codegraph-api-prod.ecs.devfactory.com/api/1.0/graphs/' + v + '/query'
        retry(1, { e -> cgErrorList.add((String[]) [k, v])}) {
            cgClient.request(Method.POST, ContentType.JSON) { req ->
                body = [query: query, querytype: 'cypher', resulttype: 'row']
                response.success = { resp, json ->
                    json.results.data.row.each {
                        it.each {
                            String completeFile = it[2];
                            String fileName = completeFile.substring(completeFile.lastIndexOf("/"), completeFile.size())
                            retViolation.add(new Violation([projectName: k, file: completeFile, fileName: fileName, line: it[1], col: it[0]]))
                        }
                    }
                }

                response.failure = { resp ->
                    println "Unexpected error: ${resp.statusLine.statusCode}"
                    println $ { resp.statusLine.reasonPhrase }
                }
            }
        }
    }
    return retViolation
}


void generateCsvPerRule() {

    violations = findAllSonarViolations()
    violations.each {
        project, rule ->
            rule.each {
                name, violation -> println project + " : Total violations: ${violation.size()}"
            }

    }
    exportToCsvPerRule(violations)

}



void exportToCsvPerRule(Map<String, Map<String, List<String[]>>> projectRuleViolations) {

    File rbf = new File(fileLocal);
    boolean diretoryCreated = rbf.mkdirs();
    if (diretoryCreated) {
        println(String.format("Directory %s has been created.", rbf))
    }
    for (Map.Entry<String, Map<String, List<String[]>>> entryProject : projectRuleViolations.entrySet()) {
        String projectName = entryProject.getKey();
        for (Map.Entry<String, List<String[]>> entryRuleViolation :
                entryProject.getValue().entrySet()) {
            String ruleName = entryRuleViolation.getKey();

            resultPath = rbf.toString() + "/" + projectName + "_" + ruleName + ".csv"
            Writer fileRbfWriter = new OutputStreamWriter(
                    new FileOutputStream(resultPath),
                    StandardCharsets.UTF_8)
            CSVWriter reportWriter = new CSVWriter(fileRbfWriter, (char) ",")

            List<String[]> violations = entryRuleViolation.getValue();
            println projectName + "," + violations.size()
            for (String[] item : violations) {
                String[] reportLine = new String[3];
                reportLine[0] = item[3]; // col
                reportLine[1] = item[1]; // line
                reportLine[2] = item[0]; //file
                reportWriter.writeNext(reportLine);
            }

            reportWriter.flush();
            reportWriter.close();
        }

    }
}

void exportToCsvSummary(String ruleName, List<Violation> sonarViolation, List<Violation> cgViolation) {

    println "Size CG Violation: " + cgViolation.size()
    println "Size Sonar Violation: " + sonarViolation.size()

    def projectCountCG = cgViolation.countBy { it.projectName }
    def projectCountSonar = sonarViolation.countBy { it.projectName }

    println ">>> FINAL RESULTS: \n"

    List<String[]> resultCsv = new ArrayList<>()

    cgProjects.each { k, v ->
        countCG = ""
        if(cgErrorList.any{it == [k,v]}){
            countCG = "Error"
        }else{
            countCG = (projectCountCG.get(k) ?: "0")
        }
        println k + "," + (projectCountSonar.get(k) ?: "0") + "," + countCG
        String[] reportLine = new String[3];
        reportLine[0] = k
        reportLine[1] = (projectCountSonar.get(k) ?: "0")
        reportLine[2] = countCG
        resultCsv.add(reportLine)
    }

    File rbf = new File(fileLocal);
    boolean diretoryCreated = rbf.mkdirs();
    if (diretoryCreated) {
        println(String.format("Directory %s has been created.", rbf))
    }

    resultPath = rbf.toString() + "/SUMMARY_" + ruleName + ".csv"
    Writer fileRbfWriter = new OutputStreamWriter(
            new FileOutputStream(resultPath),
            StandardCharsets.UTF_8)
    CSVWriter reportWriter = new CSVWriter(fileRbfWriter, (char) ",")

    reportWriter.writeNext((String[]) ['projectName', 'sonarCount', 'cgCount'])
    reportWriter.writeAll(resultCsv)
    reportWriter.flush();
    reportWriter.close();

}

void saveCGErrors(String ruleName){
    File rbf = new File(fileLocal);
    boolean diretoryCreated = rbf.mkdirs();
    if (diretoryCreated) {
        println(String.format("Directory %s has been created.", rbf))
    }

    resultPath = rbf.toString() + "/ERROR_" + ruleName + ".csv"
    Writer fileRbfWriter = new OutputStreamWriter(
            new FileOutputStream(resultPath),
            StandardCharsets.UTF_8)
    CSVWriter reportWriter = new CSVWriter(fileRbfWriter, (char) ",")

    reportWriter.writeAll(cgErrorList)
    reportWriter.flush();
    reportWriter.close();
}

void exportToCsvDiff(String ruleName, List<Violation> sonarViolation, List<Violation> cgViolation) {
    if (sonarViolation.isEmpty() && cgViolation.isEmpty()) {
        return;
    }

    def intersectViolation = sonarViolation.intersect(cgViolation)
    def onlyCG = cgViolation - sonarViolation
    def onlySonar = sonarViolation - cgViolation


    File rbf = new File(fileLocal);
    boolean diretoryCreated = rbf.mkdirs();
    if (diretoryCreated) {
        println(String.format("Directory %s has been created.", rbf))
    }

    resultPath = rbf.toString() + "/ALL_" + ruleName + ".csv"
    Writer fileRbfWriter = new OutputStreamWriter(
            new FileOutputStream(resultPath),
            StandardCharsets.UTF_8)
    CSVWriter reportWriter = new CSVWriter(fileRbfWriter, (char) ",")


    List<String[]> resultCsv = new ArrayList<>()
    intersectViolation.each {
        String[] reportLine = new String[7];
        reportLine[0] = it.projectName
        reportLine[1] = it.col
        reportLine[2] = it.line
        reportLine[3] = it.file
        reportLine[4] = it.col
        reportLine[5] = it.line
        reportLine[6] = it.file
        resultCsv.add(reportLine)
    }

    onlySonar.each {
        String[] reportLine = new String[7];
        reportLine[0] = it.projectName
        reportLine[1] = it.col
        reportLine[2] = it.line
        reportLine[3] = it.file
        reportLine[4] = '0'
        reportLine[5] = '0'
        reportLine[6] = it.file
        resultCsv.add(reportLine)
    }

    onlyCG.each {
        String[] reportLine = new String[7];
        reportLine[0] = it.projectName
        reportLine[1] = '0'
        reportLine[2] = '0'
        reportLine[3] = it.file
        reportLine[4] = it.col
        reportLine[5] = it.line
        reportLine[6] = it.file
        resultCsv.add(reportLine)
    }

    resultCsv.sort { a, b -> a[0] <=> b[0] ?: a[6] <=> b[6] ?: a[3] <=> b[3] ?: a[5] <=> b[5] ?: a[2] <=> b[2] ?: a[4] <=> b[4] ?: a[1] <=> b[1] }
    reportWriter.writeNext((String[]) ['projectName', 'sonarCol', 'sonarLine', 'sonarFile', 'cgCol', 'cgLine', 'cgFile'])
    reportWriter.writeAll(resultCsv)
    reportWriter.flush();
    reportWriter.close();

    println "===== Files not found in Sonar ====="
    println cgViolation.findAll {
        !sonarViolation.fileName.contains(it.fileName)
    }.file

    println "===== Files not found in CG ====="
    println sonarViolation.findAll {
        !cgViolation.fileName.contains(it.fileName)
    }.file

    def countCg = cgViolation.countBy { it.fileName }
    def countSonar = sonarViolation.countBy { it.fileName }

    countCg.intersect(countSonar).each {
        countCg.remove(it.key);
        countSonar.remove(it.key)
    }

    println "COUNTCG"
    println countCg

    println "COUNTSONAR"
    println countSonar



}


List<Violation> toViolationDTO(Map<String, Map<String, List<String[]>>> projectRuleViolations) {
    List<Violation> retViolationDTO = new ArrayList<>()
    for (Map.Entry<String, Map<String, List<String[]>>> entryProject : projectRuleViolations.entrySet()) {
        String projectName = entryProject.getKey();
        for (Map.Entry<String, List<String[]>> entryRuleViolation :
                entryProject.getValue().entrySet()) {
            String ruleName = entryRuleViolation.getKey();

            List<String[]> violations = entryRuleViolation.getValue();
            for (String[] item : violations) {
                String completeFile = item[0];
                String fileName2 = completeFile;
                if (completeFile.contains("/")) {
                    fileName2 = completeFile.substring(completeFile.lastIndexOf("/"), completeFile.size())
                }
                retViolationDTO.add(new Violation([projectName: projectName, file: completeFile, fileName: fileName2, line: item[1].toInteger(), col: item[3].toInteger()]))
            }
        }
    }
    return retViolationDTO
}

Map<String, Map<String, List<String[]>>> findAllSonarViolations(){

    Map<String, Map<String, List<String[]>>> allViolations = new HashMap<>();

    cgProjects.each {
        allViolations.putAll(findSonarViolationsPerProject(it.key));
    }
    return allViolations;
}

// projectName, <rule , violations <file, startLine, endLine, startOffset, endOffset, message, type>>
Map<String, Map<String, List<String[]>>> findSonarViolationsPerProject(String project) throws JSONException {

    ClientConfig cc = new DefaultClientConfig();
    cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    Client client = Client.create(cc);

    sonarUrl = sonarBaseUrl + "/api/issues/search?" + componentRoots(Collections.singletonList(project)) + "&" + ruleId

    println sonarUrl

    WebResource webResource = client.resource(sonarUrl)
            .queryParam("ps", String.valueOf(100))
            .queryParam("p", "1")
            .queryParam("statuses", "OPEN,REOPENED");
    ClientResponse response =
            webResource.accept("application/json").type("application/json").get(ClientResponse
                    .class);

    String body = response.getEntity(String.class);
    JSONObject jsonObj = new JSONObject(body);

    //println(String.format("Body %s", body));
    int total = jsonObj.getInt("total");
    int availablePages = (int) Math.ceil(total / 100);
    println(String.format("Available pages %d, and total %d", availablePages, total));

    return getMapResult(client, jsonObj, availablePages);
}

Map<String, Map<String, List<String[]>>> getMapResult(Client client, JSONObject jsonObj, int availablePages)
        throws JSONException {
    Map<String, Map<String, List<String[]>>> ruleViolations = new HashMap<>();
    ruleViolations = processResponse(ruleViolations, jsonObj);
    if (availablePages > 1) {
        for (int i = 2; i <= availablePages; i++) {
            println(String.format("Requesting page %d", i))
            WebResource webResource = client.resource(sonarUrl)
                    .queryParam("ps", String.valueOf(100))
                    .queryParam("p", String.valueOf(i));
            ClientResponse response = webResource.accept("application/json")
                    .type("application/json")
                    .get(ClientResponse.class);
            String body = response.getEntity(String.class);
            //println(String.format("paged Body is %s", body))
            JSONObject newBody = new JSONObject(body);
            ruleViolations = processResponse(ruleViolations, newBody);
        }
    }
    return ruleViolations;
}

Map<String, Map<String, List<String[]>>> processResponse(
        Map<String, Map<String, List<String[]>>> ruleViolations, JSONObject jsonObj) throws JSONException {
    JSONArray array = jsonObj.getJSONArray("issues");
    for (int i = 0; i < array.length(); i++) {
        JSONObject issueObj = (JSONObject) array.get(i);
        String projectName = (String) issueObj.get("project");
        String ruleName = (String) issueObj.get("rule");
        String file = (String) issueObj.get("component");
        //println(String.format("IssueObject is %s", issueObj))
        String[] newViolation = getStringsFromResponse(issueObj, file);
        Map<String, List<String[]>> ruleMap = ruleViolations.getOrDefault(projectName, new HashMap<>());
        List<String[]> violations = ruleMap.getOrDefault(ruleName, new ArrayList<>());
        violations.add(newViolation);
        ruleMap.put(ruleName, violations);
        ruleViolations.put(projectName, ruleMap);
        //println(String.format("New Violation for rule %s has been added for project %s", ruleName, projectName))
    }
    return ruleViolations
}

String[] getStringsFromResponse(JSONObject issueObj, String file) throws JSONException {
    String startLine = "";
    String endLine = "";
    String startOffset = "";
    String endOffset = "";
    if (issueObj.has("textRange")) {
        JSONObject textRange = (JSONObject) issueObj.get("textRange");
        startLine = String.valueOf(textRange.get("startLine"));
        endLine = String.valueOf(textRange.get("endLine"));
        if (textRange.has("startOffset")) {
            startOffset = String.valueOf(textRange.get("startOffset"));
        }
        if (textRange.has("endOffset")) {
            endOffset = String.valueOf(textRange.get("endOffset"));
        }
    }
    String message = issueObj.getString("message");
    String type = issueObj.getString("type");
    String project = issueObj.getString("project");
    String subProject = "";
    if (issueObj.has(subProject)) {
        subProject = issueObj.getString("subProject");
    }


    String fileOk = file.replaceAll(subProject + ":", '')
    return [fileOk, startLine, endLine, startOffset, endOffset, message, type]
}


String componentRoots(List<String> cgProjects) {
    String ret = "componentRoots="
    boolean comma = false;
    cgProjects.each {
        if (comma) {
            ret += new String("," + it).encodeURL()
        } else {
            comma = true;
            ret += it.encodeURL()
        }
    }
    return ret;
}

def retry(int times = 5, Closure errorHandler = { e -> log.warn(e.message, e) }
          , Closure body) {
    int retries = 0
    def exceptions = []
    while (retries++ < times) {
        try {
            println "Attempt:" + retries
            return body.call()
        } catch (e) {
            exceptions << e
            errorHandler.call(e)
        }
    }
    //throw new Exception("Failed after $times retries")
}
