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
//        'https://scm-ba.devfactory.com/scm/cap/business-payment.git'  : 'bef53d1aa446076ea0217d85ab89c99d99da7a3a',
//        'https://github.com/trilogy-group/aurea-aes-edi.git'          : 'af3dc86',
//        'https://github.com/trilogy-group/aurea-java-brp-cs-ruletest' : 'b0da0b4ed48c7f48e41a7d5304e7a8a25dd3d6ac',
//        'https://github.com/trilogy-group/aurea-lyris-platform-edge'  : '15e8699d82de854f5e4d6c40fe137056afdd9854',
//        'https://github.com/trilogy-group/aurea-sonic-mq'             : '2b8497b82efb8f2af5ba016a006d1349b224d9ea',
//        'https://github.com/trilogy-group/devfactory-codegraph-server': 'fc3450cfa4fd1cce236daf0e4dbaf891104bd8bb',
//        'https://github.com/trilogy-group/ignite-sensage-analyzer.git': 'f46a44ecbc5e95f778fb4f72cdb86509632b28d2',
//        'https://github.com/trilogy-group/kerio-mykerio-kmanager'     : 'e40fd363d019e8ff29a90cbc974818abdd4dae7c',
//        'https://github.com/trilogy-group/ta-smartleads-lms-mct.git'  : 'db45cdba9dce7cd97e721f954e1d7c1b3f0dbb6f',
//        'https://github.com/trilogy-group/versata-m1.ems.git'         : '1045056ed830b808cca82ff4d06b4d9add50064c',
//        'https://scm.devfactory.com/stash/scm/uppowersteering/pss.git': '18d5ec487055ce16a639bdd8efb7ab434ffba4bf',
//        'https://github.com/trilogy-group/wicket.git'                 : '0889d9ec569ff8a3edf17881941e52e9155bbb65',
//        'https://github.com/trilogy-group/drools.git'                 : '0a526a9876cc61374a50cf60a9c0bdf89e98ecd8',
//        'https://github.com/trilogy-group/spring-framework.git'       : '88d3526ef1fcf0dc2bc9098159d98eaf472c245f'
]

cgProjects = [
        'https://github.com/apache/maven'                                              : '4f2a2dba89251d9045fe9944783509a397491da3',
        'https://github.com/BangKindo/Beginner'                                        : '96df032a91d605b1bf3d20e9bfaa4f03f9f21a3c',
        'https://github.com/bitcoinj/bitcoinj'                                         : 'c36784b3079e5cbbcae6674304a13a56db4fcfec',
        'https://github.com/EasyRules/easyrules'                                       : '31eee54ae700a6b35f56d1223e385b65c9004ac7',
        'https://github.com/gruntjs/grunt-contrib-jshint'                              : '2d237ee472a344cefc0a2467dbccbf09c2fc2c55',
        'https://github.com/jasmine/jasmine'                                           : '8ad9abb19a7fe4e686a86b7896cd20c554a4830a',
        'https://github.com/JoshClose/CsvHelper'                                       : '9fe0bca62c30c4dac4bd2292790a41c1f454def7',
        'https://github.com/ktuukkan/marine-api'                                       : '8b1f7b87ae6abc5e72b417bfee343d39266f88e1',
        'https://github.com/madskristensen/MiniBlog'                                   : 'd6db9908dbf69ec48c8a7427b3fb1beada1796ce',
        'https://github.com/mbdavid/LiteDB'                                            : '7dad3c343a268e0669ae87aa02cf279b74d5a03a',
        'https://github.com/mgravell/protobuf-net'                                     : 'a978575b9e8bc281f7341237cace35220ef0ff35',
        'https://github.com/NancyFx/Nancy'                                             : 'b25b06e33d76da17bea1cabbfd29324ee4ee84d8',
        'https://github.com/nhibernate/nhibernate-core'                                : '9527cb075f72d8dec6150e7ef8aec17bc2a11066',
        'https://github.com/nopSolutions/nopCommerce'                                  : '7bd736f4d601cdef0c832a044a1d6677c40b8e7d',
        'https://github.com/paulcbetts/ModernHttpClient'                               : '64b36bb670ae655455477b766c60683ca0cf3a3e',
        'https://github.com/pixijs/pixi.js'                                            : 'ac502579e948086c3826d872846aae159bf75a33',
        'https://github.com/ponsonio-aurea/Leaflet'                                    : '174e693912e3eec0434af4fd9bdb96f1351bac8b',
        'https://github.com/pranet/cs_sample'                                          : '9fd1af406cf533e53807aa8dc79c0e6d9c5cf139',
        'https://github.com/rajithd-aurea/rest-api-violation'                          : '4b8af12d8565a0d27ff9ad268ac52c3cf402d2a9',
        'https://github.com/restsharp/RestSharp'                                       : 'e7c65df751427298cb59f5456bbf1f59967996be',
        'https://github.com/schambers/fluentmigrator'                                  : '42dbb50fd360fe53a97a100a6ebc9f2a8534473e',
        'https://github.com/SignalR/SignalR'                                           : 'b73cbabe69bc48c4cb8d768148e157712a264ce9',
        'https://github.com/spring-projects/aws-maven'                                 : 'ce6a4ffbd8a59029ebe147124ac2d7ff1a7afd18',
        'https://github.com/square/okhttp'                                             : '13d81f8d1b6ea1144a4a0bbbbbe03f390440f257',
        'https://github.com/TetianaMalva/begin'                                        : 'f3dc0ead066fb863dbdb2ddac5dd285414866d28',
        'https://github.com/trilogy-group/aLine-FirewalForCode'                        : '55fc54e7d65fbae15d307fe6e07c729c9d180847',
        'https://github.com/trilogy-group/aurea-ace-generix'                           : '46f1b53d3fb42054af5c316cb3e7a0a4f1ca63fb',
        'https://github.com/trilogy-group/aurea-crm-aline'                             : 'fc171554d7aa4b42f8fd40e8130d85fffff8ecaf',
        'https://github.com/trilogy-group/aurea-crm-office-addin'                      : '081091d6ecfca0576dac78339bba4cba3dc81ca0',
        'https://github.com/trilogy-group/aurea-dxsi'                                  : '19735801c58856a5d86c92723ea545c538fafea1',
        'https://github.com/trilogy-group/aurea-ipm-main'                              : 'e97b6fb8d7a6ef229e8bd142e0adf10c5d7eb60d',
        'https://github.com/trilogy-group/aurea-nextdocs-adlib'                        : '0b04c51bff471b66c105af43f994956a2167addc',
        'https://github.com/trilogy-group/aurea-sonic-mq'                              : 'f1d56b17e8f0d867ea24c1b63e9dc0e40962c5f0',
        'https://github.com/trilogy-group/bc-java.git'                                 : '7e6346c2d6b3272fc0f706f2f733f7ee90d33779',
        'https://github.com/trilogy-group/devfactory-docker-scorecard'                 : '7f9cae155e8ab206735e233df6018d8acd14ba3c',
        'https://github.com/trilogy-group/devfactory-ideplugins2.0-visualstudio-plugin': '77e9d5a68aa22a89c9decbcac3436b1db67d597a',
        'https://github.com/trilogy-group/devfactory-utbelt'                           : '7f6b93fec968c9fdc180ea2d8fc723b3360c67ab',
        'https://github.com/trilogy-group/gfi-eventsmanager'                           : 'd3d30d50f28e67dfbbb6f37d8e6b08864323994f',
        'https://github.com/trilogy-group/gfi-oneguard'                                : '94d48a8739c91fdb2922e1ddfb807a451e58365d',
        'https://github.com/trilogy-group/ignite-acorn-eps'                            : '75caed3f299a922b9cd985f547598c52e3a1aede',
        'https://github.com/trilogy-group/kerio-connect-connect'                       : 'e650703a9faf4486eed9190873f90fe62e29310b',
        'https://github.com/trilogy-group/kerio-winroute-at-mykerio'                   : '9b3942380f965b77d3424bc3a967c51697a32d01',
        'https://github.com/trilogy-group/QuickSearch-demo'                            : '3981a483b3e85887d6338bf1d56780b8b3986704',
        'https://github.com/trilogy-group/versata-epmlive-epml-c2'                     : '8f4b075fddef3cf01f61222fc219590f0602c031',
        'https://github.com/trilogy-group/versata-m1.client-ems-client'                : '20683b4d4879f207261a58812da8c67e06dde861',
        'https://scm-ba.devfactory.com/scm/cap/business-avios'                         : '9fc49cc2aacb6b78c5110456e02d0bab6423b34f',
        'https://scm-ba.devfactory.com/scm/cap/business-framework'                     : 'b79d431292d96e5d55cce749b95a456eddc29301',
        'https://scm-ba.devfactory.com/scm/cap/business-redemptions-builders'          : '8f53747d269c3e3973023b10b355b8f9fc7391b7',
        'https://scm-ba.devfactory.com/scm/cap/reservations-common'                    : 'b48e9834efa458741bd812a1e93b9eb6563d2ace',
        'https://scm-ba.devfactory.com/scm/cap/web-contextualisation'                  : '10b25a42f8f6b65b582e5a63aac8d901a1fc5404',
        'https://scm-ba.devfactory.com/scm/cap/web-diagtool'                           : '473625e1d665a341e4f515e0ea13c7e75a173a8a',
        'https://scm-ba.devfactory.com/scm/cap/web-framework-schema'                   : '1ed51eb219321200887145347563ed4598870216',
        'https://scm-ba.devfactory.com/scm/cap/web-payment'                            : '6b3096cbe81adc12259b6b5b2fb5e8e7449dbd37',
        'https://scm-ba.devfactory.com/scm/cap/web-selling-builders'                   : 'e3811a9f0632fba218e72d768bf1919303651178',
        'https://scm-ba.devfactory.com/scm/cap/web-sitenavigation'                     : '726b4cddc656076a1aef0d4439397498415524bc',
        'https://scm-ba.devfactory.com/scm/cap/web-xmlhttp-proxy'                      : 'bc482cb62c7dd38937aefb84bb52d538bcc05921',
        'https://scm-ba.devfactory.com/scm/cap2/fc-baflt-bafma.git'                    : 'd2fe293f2971800150da0bac065d9175ecfe46b5',
        'https://scm-ba.devfactory.com/scm/cap2/fc-ndc-cma'                            : '0e0c47cf615f1b65d03435d79cfa68d7f4fcb62d',
        'https://scm-ba.devfactory.com/scm/cap2/fc-pym-vpa'                            : '31230b18ecf795dfcdd6235abebfe0f663c4c829',
        'https://scm-ba.devfactory.com/scm/cap2/fc-sea-sesa'                           : '92124eed35c3bcf2e9573ffc2b02115bcaba9b18',
        'https://scm-ba.devfactory.com/scm/cap2/mp-evm-evtm'                           : '4ad392b07e767ea6873ae31d6178a343513596b5',
        'https://scm-ba.devfactory.com/scm/cap2/services-aaui-cpm'                     : 'c816cdf65883e5f200510b731dbf49a0ba7e775a',
        'https://scm-ba.devfactory.com/scm/cap2/services-acd-emdm'                     : 'a5709133063ee5bfdbb49dddf429a5ab0873e16e',
        'https://scm-ba.devfactory.com/scm/cap2/services-asm-ema'                      : '251777899066f29c88bdd462466db734772306a6',
        'https://scm-ba.devfactory.com/scm/cap2/services-baflt-bamsr'                  : '110e67bf0073e8ad5ea911309ee830321a6e58b3',
        'https://scm-ba.devfactory.com/scm/cap2/services-bds-mdp'                      : '3ce4ef2c13993917e6ed71eb813b5f611307c838',
        'https://scm-ba.devfactory.com/scm/cap2/services-car-cav'                      : '421b62c3273725bf910714511a3a91e501a4b08e',
        'https://scm-ba.devfactory.com/scm/cap2/services-cargopub-cargopub'            : '8cf4d1f24998732e6181f5c6d23ed96c0ce81ddd',
        'https://scm-ba.devfactory.com/scm/cap2/services-cem-cpr'                      : '5a8fb3ecbe728ecf2a13c00d7f0b9e965c916d61',
        'https://scm-ba.devfactory.com/scm/cap2/services-cmg-ccb'                      : '8b46b676d3af1896d5b341bf2d0df00a87adc5ea',
        'https://scm-ba.devfactory.com/scm/cap2/services-concur-tem'                   : 'fc388c45f31ea367637d7ec1a1ec721288e5969f',
        'https://scm-ba.devfactory.com/scm/cap2/services-core-ehcache'                 : 'fff46870b34a97fdaa5edfde3c1ab516a131259e',
        'https://scm-ba.devfactory.com/scm/cap2/services-crw-ccbt'                     : '68340a8fd75430d2c40b20e63c1356d4df1d0ad7',
        'https://scm-ba.devfactory.com/scm/cap2/services-dvm-tcm'                      : '69290b7cacaf30b7ff1801d74cfb07f8be82819d',
        'https://scm-ba.devfactory.com/scm/cap2/services-fli-cbu'                      : 'd74ec32c4342e8a41f642f2dc5847d111487dd74',
        'https://scm-ba.devfactory.com/scm/cap2/services-fom-foma'                     : '5e4c21a9bfd92406de87bf6a8f0b232306d12ec8',
        'https://scm-ba.devfactory.com/scm/cap2/services-iata-ssba'                    : 'd52b71c974014af21f67b0a24ee2a837e706b3a3',
        'https://scm-ba.devfactory.com/scm/cap2/services-meo-lca'                      : 'ba44d4679e423bb8924d3433b42cfdea0c65d233',
        'https://scm-ba.devfactory.com/scm/cap2/services-ndc-dist'                     : '66c9139c53ac8433fb8d6b748f98cf39b5e0714f',
        'https://scm-ba.devfactory.com/scm/cap2/services-orm-ordrt'                    : '4df741108f943f404b577d9b8a51178e8fdf35d0',
        'https://scm-ba.devfactory.com/scm/cap2/services-pega-cma'                     : 'f71c7d287e01d504dff36070f48222347201512e',
        'https://scm-ba.devfactory.com/scm/cap2/services-sas-mfas'                     : 'e8bc0600c2222c0e1f1d84de9f052db13e631cec',
        'https://scm-ba.devfactory.com/scm/cap2/services-sse-invm'                     : 'c9d3b4a992543db106096423f155fb1168890270',
        'https://scm-ba.devfactory.com/scm/cap2/services-sse-sbkm'                     : '9a05872749e29b3ebbf525e84794396d5bc4335e',
        'https://scm-ba.devfactory.com/scm/cap2/services-svrm-manc'                    : '5fe7f4d48b14a4306f1c6d3b2392fecee80ed787',
       'https://scm.devfactory.com/stash/scm/crossover/bandcamp'                      : '7da12747557379225da9f6046e43b39107006b87',
        'https://scm.devfactory.com/stash/scm/upland-eclipse/eclipse'                  : 'fc8e2481c50e4f3249669428625a583c1600448e',
        'https://scm.devfactory.com/stash/scm/upland-filebound/leadlander'             : '3b43a49726c10dbd3faf038f1ed0c36111a8b5da'
]

//cgProjects = [
//        'https://github.com/trilogy-group/ignite-acorn-aaa'                         : '',
//        'https://github.com/trilogy-group/ignite-acorn-pa5g'                        : '',
//        'https://scm.devfactory.com/stash/scm/dfidep/dfideplugins'                  : '',
//        'https://github.com/trilogy-group/aurea-nextdocs-nextdocs61x'               : '',
//        'https://scm.devfactory.com/stash/scm/crossover/dotnettracker'              : '',
//        'https://github.com/trilogy-group/gfi-mail-archiver'                        : '',
//        'https://github.com/trilogy-group/gfi-directory-service'                    : '',
//        'https://github.com/trilogy-group/gfi-endpointsecurity'                     : '',
//        'https://github.com/trilogy-group/gfi-eventsmanager'                        : '',
//        'https://github.com/trilogy-group/gfi-faxmaker-online'                      : '',
//        'https://github.com/trilogy-group/gfi-faxmaker-server'                      : '',
//        'https://github.com/trilogy-group/gfi-languard'                             : '',
//        'https://github.com/trilogy-group/gfi-mail-essentials'                      : '',
//        'https://github.com/trilogy-group/gfi-oneconnect-M1-EMSClient'              : '',
//        'https://github.com/trilogy-group/gfi-oneguard'                             : '',
//        'https://github.com/trilogy-group/gfi-web-monitor'                          : '',
//        'https://scm.devfactory.com/stash/scm/aurea-dfi/dotnet-importer'            : '',
//        'https://github.com/trilogy-group/versata-m1.client-ems-client'             : '',
//        'https://scm.devfactory.com/stash/scm/upland-filebound/leadlander.git'      : '',
//        'https://scm.devfactory.com/stash/scm/upland-filebound/filebound_v7'        : '',
//        'https://scm.devfactory.com/stash/scm/upland/filebound_v7'                  : '',
//        'https://scm.devfactory.com/stash/scm/upland-filebound/projectlviv'         : '',
//        'https://scm.devfactory.com/stash/scm/upland-filebound/filebound_capture_v7': '',
//        'https://scm.devfactory.com/stash/scm/UPLAND-ECLIPSE/eclipse-tfs-2-git'     : '',
//        'https://github.com/trilogy-group/vepmlive-epmlive2013release'              : '',
//        'https://github.com/johnsonz/MvcContosoUniversity'                          : '',
//        'https://github.com/NancyFx/Nancy'                                          : '',
//        'https://github.com/zeromq/netmq'                                           : '',
//        'https://github.com/JamesNK/Newtonsoft.Json'                                : '',
//        'https://github.com/nhibernate/nhibernate-core'                             : '',
//        'https://github.com/nunit/nunit'                                            : '',
//        'https://github.com/restsharp/RestSharp'                                    : '',
//        'https://github.com/markrendle/Simple.Data'                                 : '',
//        'https://github.com/danielpalme/MVCBlog'                                    : '',
//        'https://github.com/JoshClose/CsvHelper'                                    : '',
//        'https://github.com/Reactive-Extensions/Rx.NET'                             : '',
//        'https://github.com/NSwag/NSwag'                                            : '',
//        'https://github.com/jagregory/fluent-nhibernate'                            : '',
//        'https://github.com/mongodb/mongo-csharp-driver'                            : '',
//        'https://github.com/paulcbetts/refit'                                       : '',
//        'https://github.com/paulcbetts/ModernHttpClient'                            : '',
//        'https://github.com/ravendb/ravendb'                                        : '',
//        'https://github.com/mbdavid/LiteDB'                                         : '',
//        'https://github.com/aspnet/Microsoft.Data.Sqlite'                           : '',
//        'https://github.com/aspnet/EntityFramework6'                                : '',
//        'https://github.com/aspnet/Razor'                                           : '',
//        'https://github.com/aspnet/dotnet-watch'                                    : '',
//        'https://github.com/aspnet/MusicStore'                                      : '',
//        'https://github.com/aspnet/NerdDinner'                                      : '',
//        'https://github.com/NancyFx/Nancy.Blog'                                     : '',
//        'https://github.com/NancyFx/Nancy.Demo.Samples'                             : '',
//        'https://github.com/AvaloniaUI/Avalonia'                                    : '',
//        'https://github.com/StackExchange/dapper-dot-net'                           : '',
//        'https://github.com/AutoMapper/AutoMapper'                                  : '',
//        'https://github.com/Redth/PushSharp'                                        : '',
//        'https://github.com/PavelTorgashov/FastColoredTextBox'                      : '',
//        'https://github.com/SignalR/SignalR'                                        : '',
//        'https://github.com/opserver/Opserver'                                      : '',
//        'https://github.com/reactiveui/ReactiveUI'                                  : '',
//        'https://github.com/cefsharp/CefSharp'                                      : '',
//        'https://github.com/NLog/NLog'                                              : '',
//        'https://github.com/HangfireIO/Hangfire'                                    : '',
//        'https://github.com/FransBouma/Massive'                                     : '',
//        'https://github.com/JeremySkinner/FluentValidation'                         : '',
//        'https://github.com/StackExchange/StackExchange.Redis'                      : '',
//        'https://github.com/quartznet/quartznet'                                    : '',
//        'https://github.com/Topshelf/Topshelf'                                      : '',
//        'https://github.com/kevin-montrose/Jil'                                     : '',
//        'https://github.com/schambers/fluentmigrator'                               : '',
//        'https://github.com/akavache/Akavache'                                      : '',
//        'https://github.com/PerfDotNet/BenchmarkDotNet'                             : '',
//        'https://github.com/Antaris/RazorEngine'                                    : '',
//        'https://github.com/ServiceStack/ServiceStack.Redis'                        : '',
//        'https://github.com/aspnet/KestrelHttpServer'                               : '',
//        'https://github.com/OrchardCMS/Orchard'                                     : '',
//        'https://github.com/NEventStore/NEventStore'                                : '',
//        'https://github.com/jstedfast/MailKit'                                      : '',
//        'https://github.com/MediaBrowser/Emby'                                      : '',
//        'https://github.com/mgravell/protobuf-net'                                  : '',
//        'https://github.com/npgsql/npgsql'                                          : '',
//        'https://github.com/CollaboratingPlatypus/PetaPoco'                         : '',
//        'https://github.com/EasyNetQ/EasyNetQ'                                      : '',
//        'https://github.com/ServiceStack/ServiceStack.Text'                         : '',
//        'https://github.com/sjdirect/abot'                                          : '',
//        'https://github.com/madskristensen/MiniBlog'                                : '',
//        'https://github.com/nopSolutions/nopCommerce'                               : '',
//        'https://github.com/oxyplot/oxyplot'                                        : '',
//        'https://github.com/MarlabsInc/SocialGoal'                                  : '',
//        'https://github.com/beto-rodriguez/Live-Charts'                             : '',
//        'https://github.com/smartstoreag/SmartStoreNET'                             : '',
//        'https://github.com/smsohan/MvcMailer'                                      : '',
//        'https://github.com/StackExchange/NetGain'                                  : '',
//        'https://github.com/fluentscheduler/FluentScheduler'                        : '',
//        'https://github.com/DotNetAnalyzers/StyleCopAnalyzers'                      : '',
//        'https://github.com/Knagis/CommonMark.NET'                                  : '',
//        'https://github.com/pascalabcnet/pascalabcnet'                              : '',
//        'https://github.com/PowerPointLabs/PowerPointLabs'                          : '',
//        'https://github.com/naudio/NAudio'                                          : '',
//        'https://github.com/apache/logging-log4net'                                 : ''
//]

println ">>> SCRIPT STARTED <<<"

urlCG = 'http://codegraph-api-prod.ecs.devfactory.com/api/1.0/graphs?status=Completed&active=true'

println ">>> URL CODEGRAPH: " + urlCG
println ">>> GMT:" + new Date().toGMTString()

updateDatabase = true; //CHANGE HERE

showOffline = true
showOnline = true

def cgClient = new RESTClient(urlCG)
cgClient.getClient().params.setParameter("http.connection.timeout", 20000)
cgClient.getClient().params.setParameter("http.socket.timeout", 21000)

cgProjects.each { sourceUrl, revision ->
    retry(3, { e -> e.printStackTrace() }) {
        cgClient.request(Method.GET) {
            response.success = { resp, json ->
                processResponse(json, sourceUrl, revision)
                //processResponseGetOffline(json)
            }

            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode}"
                println $ { resp.statusLine.reasonPhrase }
            }
        }
    }
}


private void processResponseGetOffline(json) {
    def ret = json.findAll {
        it.find { key, value ->
            key == 'deployStatus' &&
                    value != 'Online'
        }
    }

    ret.each { it -> println(it.get('requestId') + it.get('deployStatus')) }
}

private void processResponse(json, sourceUrl, revision) {
    def ret = json.findAll {
        it.find { key, value ->
            value == sourceUrl
        } &&
                it.find { key, value ->
                    value == revision
                }
    }

    if (ret.size() == 0) {
        println ">>> " + sourceUrl + " : " + revision + " : " + " - NOT FOUND IN CODEGRAPH."
        return
    }

    if (ret.size() > 1) {
        ret = ret.sort {
            it.get('commitDate')
        }.reverse()
    }

    def cgProject = ret[0]

    try {
        if (!cgProject['neo4jUrl']?.trim()) {
            if (showOffline) println ">>> " + sourceUrl + " : " + revision + " : NO PORT ASSIGNED - OFFLINE. requestId: " + cgProject['requestId']
            return
        }

        def neo4jClient = new RESTClient(cgProject['neo4jUrl'])
        neo4jClient.getClient().params.setParameter("http.connection.timeout", 20000)
        neo4jClient.getClient().params.setParameter("http.socket.timeout", 21000)

        neo4jClient.request(Method.GET) {
            response.success = { respNeo, jsonNeo ->
                if (showOnline) println ">>> " + sourceUrl + " : " + revision + " : " + cgProject['neo4jUrl'] + " - ONLINE. requestId: " + cgProject['requestId']

                if (updateDatabase) {
                    updateDB(sourceUrl, revision, cgProject)
                }
            }

            response.failure = { respNeo ->
                if (showOffline) println ">>> " + sourceUrl + " : " + revision + " : " + cgProject['neo4jUrl'] + " - OFFLINE. requestId: " + cgProject['requestId'] + " serviceName: " + cgProject['serviceName']
            }
        }
    } catch (Exception ex) {
        if (showOffline) println ">>> " + sourceUrl + " : " + revision + " : " + cgProject['neo4jUrl'] + " - OFFLINE. requestId: " + cgProject['requestId'] + " serviceName: " + cgProject['serviceName']
    }
}


private updateDB(String sourceUrl, String revision, Map cgProject) {

    def dbUrl = 'jdbc:mysql://devfactory-aurora-1.cluster-cd1ianm7fpxp.us-east-1.rds.amazonaws.com';
    def dbPort = 3306
    def dbSchema = 'javabrp_harness_tst'
    def dbUser = 'javabrp_harness'
    def dbPassword = '9&4i0@&k98Wp'
    def dbDriver = 'com.mysql.jdbc.Driver'

    Sql.withInstance(dbUrl + ':' + dbPort + '/' + dbSchema, dbUser, dbPassword, dbDriver) { sql ->

        def verifyIds = "SELECT cb.id AS cbId, " +
                "neodb.id AS neodbId " +
                "FROM neo4jdatabases neodb " +
                "INNER JOIN codebases cb ON (neodb.Codebase_id = cb.id) " +
                "WHERE cb.RepoUrl LIKE CONCAT(:REPO_URL, '%') "

        if (revision?.trim()) {
            verifyIds += "AND cb.revision = :REVISION "
        } else {
            verifyIds += "AND cb.revision IS NULL "
        }


        boolean hasUpdate = false
        sql.eachRow(verifyIds, REPO_URL: sourceUrl - '.git', REVISION: revision) {
            row ->
                def updateCodebases = 'UPDATE codebases ' +
                        'SET CodeGraphDbId = :CODEGRAPH_DB_ID ' +
                        'WHERE id = :CB_ID'

                def countCodebases = sql.executeUpdate updateCodebases,
                        CODEGRAPH_DB_ID: cgProject['requestId'],
                        CB_ID: row.cbId

                def updateNeo4jdatabases = 'UPDATE neo4jdatabases ' +
                        ' SET CodeGraphDbId = :CODEGRAPH_DB_ID ,' +
                        ' BoltPort = :BOLT_PORT,' +
                        ' HttpPort = :HTTP_PORT,' +
                        ' Host = :HOST,' +
                        ' DatabaseUrl = :DB_URL,' +
                        ' CodeGraphDbId = :CODEGRAPH_DB_ID' +
                        ' WHERE id = :NEODB_ID;'

                def countNeo4jdatabases = sql.executeUpdate updateNeo4jdatabases,
                        CODEGRAPH_DB_ID: cgProject['requestId'],
                        BOLT_PORT: cgProject['bolt'],
                        HTTP_PORT: cgProject['http'],
                        HOST: cgProject['host'],
                        DB_URL: cgProject['neo4jUrl'],
                        NEODB_ID: row.cbId

                hasUpdate = true

                println ">>> Test harness DB updated successfully. RowId: " + row.cbId +
                        " Codebases Total: " + countCodebases +
                        " Neo4jdatabases Total: " + countNeo4jdatabases
        }

        if (!hasUpdate) {
            println ">>> NO UPDATE: " + sourceUrl + " : " + revision
        }

    }
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
