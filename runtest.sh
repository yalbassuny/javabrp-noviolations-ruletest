#!/usr/bin/env bash
export DOCKER_HOST="tcp://webserver.devfactory.com"
if ! docker service ps sonar_rule_test ; then
    docker run -d --name sonar_rule_test -p 17805:9000 -p 17806:9092 registry2.swarm.devfactory.com/devfactory/java-brp-sonar:1
fi
printf 'Waiting for sonar response'
until $(curl --output /dev/null --silent --head --fail http://webserver.devfactory.com:17805); do
    printf '.'
    sleep 5
done
sonar-scanner -Dsonar.host.url=http://webserver.devfactory.com:17805 -Dsonar.projectKey="ruletest:project" -Dsonar.projectName="Rule Test" -Dsonar.projectVersion="1.0" -Dsonar.sources="./src/main/java" -Dsonar.java.binaries="./build/classes" -Dsonar.java.libraries="./sonarlib"

sonar-scanner -Dsonar.host.url=http://webserver.devfactory.com:17805 -Dsonar.projectKey="cassandratest:project" -Dsonar.projectName="cassandratest" -Dsonar.projectVersion="1.0" -Dsonar.sources="./src" -Dsonar.java.binaries="./cassandrabin" -Dsonar.java.libraries="./lib"