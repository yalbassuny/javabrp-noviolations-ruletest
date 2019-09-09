@GrabResolver(name = 'devfactory', root = 'https://scm.devfactory.com/nexus/content/groups/public')
@Grab('com.opencsv:opencsv:3.9')
@Grab('com.sun.jersey:jersey-bundle:1.19.3')
@Grab('org.json:json:20170516')
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@Grab('mysql:mysql-connector-java:5.1.39')
@Grab('com.devfactory.codeserver:codeserver2-client-lib:2.5.96')
@GrabConfig(systemClassLoader = false)

import com.devfactory.codeserver.client.CodeServerClient
import com.devfactory.codeserver.model.Commit
import com.devfactory.codeserver.model.Insight
import com.devfactory.codeserver.model.InsightResult

import java.time.OffsetDateTime
import java.time.ZoneOffset

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



String.metaClass.encodeURL = {
    java.net.URLEncoder.encode(delegate, "UTF-8")
}

println ">>> SCRIPT STARTED <<<"

CodeServerClient codeServerClient = CodeServerClient.builder()
        .withBaseUrl(URI.create("http://codeserver.devfactory.com"))
        .build();

Insight insight = codeServerClient.insights().getAppInsight(11, 299)



List<Commit> commitList =
        codeServerClient.commits().withLimit(2000).listAllCommits(URI.create("https://github.com/trilogy-group/codegraph.git"))


println commitList.size()

List<Insight> listInsight = codeServerClient.insights().getAppsInsights(11)

listInsight.forEach({ins ->
    List<InsightResult> retList = new ArrayList<>();
    retList.addAll(codeServerClient.insights()
                .getAppInsightResults(11, ins.getId(), null, Collections.singletonList("endDate>"+OffsetDateTime.of(2017,01,1,0, 0, 0, 0, ZoneOffset.UTC).toString()), Integer.MAX_VALUE))
    println ins.getId() + "-" + ins.getName() + "-" + retList.stream().filter({ result ->
        result.getEndDate() != null }).count()
})


//retList.stream().filter({ result ->
//    result.getEndDate() != null &&
//    result.getEndDate().isAfter(OffsetDateTime.of(2017,11,1,0, 0, 0, 0, ZoneOffset.UTC))
//}).forEach({ result ->
//    println result
//})

//Map<Revision, List<InsightResultInfo>> ret = new HashMap<>();
//commitList.forEach({ commit ->
//    println commit.getRevision().getRev()
//    ret.putAll(codeServerClient.insightResults().withLimit(Integer.MAX_VALUE)
//            .getAddedInsightResults("https://github.com/trilogy-group/codegraph.git",
//            commit.getRevision().getRev(),
//            commit.getRevision().getRev(),
//            Collections.singleton(insight)))
//})
//
//ret.forEach({k,v ->
//    println k.getRev()
//    v.forEach({result ->
//        println result.id
//    })
//})

//List<InsightResultSummary> insightResultSummaryList =
//        codeServerClient.insightResultsSummary().withLimit(Integer.MAX_VALUE)
//                .summarizeInsightResults("https://github.com/trilogy-group/codegraph.git", null, null, null)

//Integer result = insightResultSummaryList.stream().filter({it->it.getGroup().contains("javabrp") && it.getCount()!=null})
//        .collect(Collectors.summingInt({it -> it.getCount()}))
//
//println result
//insightResultSummaryList.forEach({it -> println it})

