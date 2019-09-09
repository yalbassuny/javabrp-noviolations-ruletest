import groovy.sql.Sql
@Grab('com.opencsv:opencsv:3.9')
@Grab('com.sun.jersey:jersey-bundle:1.19.3')
@Grab('org.json:json:20170516')
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@Grab('mysql:mysql-connector-java:5.1.39')
@GrabConfig(systemClassLoader = true)

import groovyx.net.http.Method
import groovyx.net.http.RESTClient

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

cgProjects = [
        'apache-mavengit':'r-3da258820d924a782b',
        'BangKindo-Beginner':'r-b47bb05c38c7028df9',
        'bitcoinj-bitcoinjgit':'r-fed227f8b2584dc356',
        'EasyRules-easyrules':'r-042ac91cc750a259f4',
        'ktuukkan-marine-api':'r-49ff382c931277629c',
        'rajithd-aurea-rest-api-violationgit':'r-909ef67053d0f5ad84',
        'spring-projects-aws-mavengit':'r-a70941765a172db90f',
        'square-okhttpgit':'r-102cab07f1426ffe6f',
//        'TetianaMalva-begin':'74f30e9f-b770-4597-b054-3ab27b2bb647',
//        'trilogy-group-aLine-FirewalForCode':'b2733b68-8e95-4b37-9f3a-77a8232e5c31',
        'trilogy-group-aurea-dxsi':'r-3e23cf8981ed82307e',
        'trilogy-group-aurea-ipm-main':'r-b6121611f154391cdb',
//        'trilogy-group-aurea-sonic-mq':'e882df5c-0aff-4ac8-bd7b-93715dcd4464',
//        'bc-java':'b9e80244-3f75-4d37-a815-3db32ee75914',
//        'trilogy-group-devfactory-docker-scorecardgit':'0a80a437-8ff8-468e-8471-5b7340bb2937',
//        'trilogy-group-devfactory-utbelt':'d0bf495e-5e33-47ce-89fc-78ba0cbe39b9',
//        'trilogy-group-kerio-connect-connectgit':'f1d4541e-01fc-4b46-9af9-94a22287a398',
//        'trilogy-group-kerio-winroute-at-mykerio':'548bf803-621f-4f96-a18f-ed13893bdf83',
//        'trilogy-group-QuickSearch-demogit':'e06f314a-92b6-443a-8888-80aa435e787d',
//        'trilogy-group-versata-m1client-ems-client':'40cff6e2-d52f-4392-97da-19bcb05a937f',
//        '[BA] cap-business-aviosgit': 'efc08422-e8d9-4029-801a-4fc7a41c2b40',
//        '[BA] cap-business-frameworkgit':'f1c8e9b0-acd6-4ba6-a157-60f012cf8d05',
//        '[BA] cap-business-redemptions-buildersgit':'c817cc98-2309-4a14-b67d-10689e7bef31',
//        '[BA] cap-reservations-commongit':'6492068c-d190-4e31-a604-39f67580757a',
//        '[BA] cap-web-contextualisationgit':'a0beff30-84d0-4d5e-827a-c0d283298d15',
//        '[BA] cap-web-paymentgit':'a56af317-2714-44aa-89a9-b297ef879356',
//        '[BA] cap-web-selling-buildersgit':'d4bf886e-c79b-4969-85e9-da55b877513d',
//        '[BA] cap-web-sitenavigationgit':'713f5991-fcdc-48b6-8ab2-6a80ceafe0d9',
//        '[BA] cap2-fc-baflt-bafmagit':'aaccd39c-5719-4f5f-98db-9d3848a63e08',
//        '[BA] cap2-fc-ndc-cmagit':'dcaac279-2ea7-40af-bd8f-8dadf056f0d3',
//        '[BA] cap2-fc-pym-vpagit':'af0c72a1-fdaf-4605-8a5e-27f5ea80cf85',
//        '[BA] cap2-fc-sea-sesagit':'3f7c2258-7a27-4bcc-8ff6-4830aadc508f',
//        '[BA] cap2-mp-evm-evtmgit':'77c387d6-9438-45c1-bdfb-d256877973d0',
//        '[BA] cap2-services-aaui-cpmgit':'0e44c48f-c2db-435a-89fd-3827d3a0036f',
//        '[BA] cap2-services-acd-emdmgit':'1e545b4c-becc-42a7-9228-39861d63f01e',
//        '[BA] cap2-services-asm-emagit':'10cb5b1e-22cc-41b9-b117-e56a5f3122f6',
//        '[BA] cap2-services-baflt-bamsrgit':'82d49705-cb88-4e4c-81e1-a5f4627b4f43',
//        '[BA] cap2-services-bds-mdpgit':'d6394f3a-4f71-47b0-9429-98dea78ac4b5',
//        '[BA] cap2-services-car-cavgit':'34d21b94-3965-4c22-b2ea-9b9b25a6576d',
//        '[BA] cap2-services-cargopub-cargopubgit':'90fd3c0e-07d1-402e-9c90-7a55bc59cadf',
//        '[BA] cap2-services-cem-cprgit':'94f8c38c-45e6-4463-b77c-16e570b16caa',
//        '[BA] cap2-services-cmg-ccbgit':'c12d78a5-805a-4356-b220-ae2a0305199a',
//        '[BA] cap2-services-concur-temgit':'ed6dac6d-c22e-4419-96c9-9365bd9f7190',
//        '[BA] cap2-services-core-ehcachegit':'436bb5e7-e05e-434e-9f65-a6ef51865e91',
//        '[BA] cap2-services-crw-ccbtgit':'03bb9e8b-0486-4d3f-8868-150012dcc670',
//        '[BA] cap2-services-dvm-tcmgit':'5e904e1d-6892-4835-a848-ece7427ba61d',
//        '[BA] cap2-services-fli-cbugit':'620bdc9a-e60c-4a9d-b86f-abc893e21194',
//        '[BA] cap2-services-fom-fomagit':'2524beca-5d2a-4982-9232-15a8b24198c5',
//        '[BA] cap2-services-iata-ssbagit':'39077240-aac0-47a0-b610-01d09366ce61',
//        '[BA] cap2-services-meo-lcagit':'80d373c6-030b-4dc8-9ca5-6fd1219be930',
//        '[BA] cap2-services-ndc-distgit':'b0d2c496-d0fd-4ddc-9966-d55cd2907f54',
//        '[BA] cap2-services-orm-ordrtgit':'2603d332-33a1-4a93-80e1-13455787a96c',
//        '[BA] cap2-services-pega-cmagit':'72aee8fb-127e-48cf-b6f7-8e920552e8c1',
//        '[BA] cap2-services-sas-mfasgit':'fa2cb5c7-e22f-43d8-a643-3fd54943d29e',
//        '[BA] cap2-services-sse-invmgit':'87b5895d-6f78-4444-bb0f-45f183c2d5c2',
//        '[BA] cap2-services-sse-sbkmgit':'04dc161f-3426-48e7-8736-02b6835e9d38',
//        '[BA] cap2-services-svrm-mancgit':'5e180dc6-ad05-4255-9609-35ec935d9951',
//        'crossover-bandcampgit':'554ec142-4cca-4ab8-8e85-b3d34678579b',
//        'messageone-m1-emsgit':'897b114d-b88a-423a-8666-28de1237ffee'
]



println ">>> SCRIPT STARTED <<<"

cgProjects.each { name, requestid ->
    process(name,requestid);
}


private process(String name, String requestId) {

    def dbUrl = 'jdbc:mysql://devfactory-aurora-1.cluster-cd1ianm7fpxp.us-east-1.rds.amazonaws.com';
    def dbPort = 3306
    def dbSchema = 'stg_brp'
    def dbUser = 'stg_brp'
    def dbPassword = 'datastg123'
    def dbDriver = 'com.mysql.jdbc.Driver'

    Sql.withInstance(dbUrl + ':' + dbPort + '/' + dbSchema, dbUser, dbPassword, dbDriver) { sql ->

        def verifyIds = "select  distinct count(v.id) as count from brp_data b " +
                "inner join violation_data v where (v.brp_data_id = b.id) " +
                "and request_id = :REQUEST_ID " +
                "and issue_key = 'S1193'"

        sql.eachRow(verifyIds, REQUEST_ID: requestId) {
            row ->
                println name + "," + row.count
        }
    }
}


