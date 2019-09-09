#!/bin/bash

mkdir -p /home/tmp

export SONAR_SCANNER_OPTS="-Djava.io.tmpdir=/home/tmp" 
export SONAR_USER_HOME=/home

for d in */; do
	rm -f $d/sonar-project.properties
    exec 3<> $d/sonar-project.properties
	echo "sonar.projectKey = ${d::-1}" >&3
	echo "sonar.projectName = ${d::-1}" >&3
	echo "sonar.projectVersion = 1.0" >&3
	echo "sonar.host.url = http://brp-sonar.ecs.devfactory.com/" >&3
	echo "sonar.sources=." >&3
	echo "sonar.language=java" >&3
	echo "sonar.sourceEncoding=UTF-8" >&3
	echo "sonar.profile=brp_profile_janoni" >&3
	echo "sonar.libraries=/home/flatlib" >&3
    exec 3>&-
    (cd $d && /home/sonar-scanner-2.8/bin/sonar-scanner)
done


