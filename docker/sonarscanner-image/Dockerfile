FROM openjdk:8

MAINTAINER Alexandre M. Janoni <alexandre.janoni@aurea.com>



ENV SONARSCANNER_HOME /home 

WORKDIR $SONARSCANNER_HOME

RUN set -x \
    && apt-get update \
    && apt-get install -y unzip --no-install-recommends \
    && apt-get install -y vim --no-install-recommends \
    && apt-get install -y nano --no-install-recommends \
    && apt-get install -y zip --no-install-recommends 


# Gradle
RUN wget https://services.gradle.org/distributions/gradle-3.4-bin.zip
RUN unzip gradle-3.4-bin.zip
RUN mv gradle-3.4 /opt/
RUN rm gradle-3.4-bin.zip
ENV GRADLE_HOME /opt/gradle-3.4
ENV PATH $PATH:$GRADLE_HOME/bin

# get maven 3.3.9
RUN wget --no-verbose -O /tmp/apache-maven-3.3.9.tar.gz http://archive.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
RUN tar xzf /tmp/apache-maven-3.3.9.tar.gz -C /opt/
RUN ln -s /opt/apache-maven-3.3.9 /opt/maven
RUN ln -s /opt/maven/bin/mvn /usr/local/bin
RUN rm -f /tmp/apache-maven-3.3.9.tar.gz
ENV MAVEN_HOME /opt/maven


RUN set -x \
	&& git config --global user.email "ajanoni@gmail.com" \
	&& git clone -b develop https://1528affcb8a1f8edd21a706df6ad2d40f7cfb23f@github.com/trilogy-group/aurea-java-brp-cs /home/java-brp-cs-10 \
	&& cd /home/java-brp-cs-10/import2cg \
	&& gradle build -x checkstyleMain -x checkstyleTest -x pmdMain -x pmdTest -x findbugsMain -x findbugsTest \
	&& cp /home/java-brp-cs-10/import2cg/build/resources/main/system.properties /home/java-brp-cs-10/import2cg \
	&& cd /home \ 
	&& wget https://sonarsource.bintray.com/Distribution/sonar-scanner-cli/sonar-scanner-2.8.zip  \
	&& wget http://mirror.nbtelecom.com.br/apache//ant/binaries/apache-ant-1.10.1-bin.zip  \
	&& unzip sonar-scanner-2.8.zip  \
	&& unzip apache-ant-1.10.1-bin.zip  \
 	&& export MAVEN_HOME=/usr/share/maven/bin  \
 	&& export ANT_HOME=/home/apache-ant-1.10.1 \ 
 	&& export PATH=$ANT_HOME/bin:$PATH \
 	&& export PATH=/home/sonar-scanner-2.8/bin:$PATH 

ENV SONAR_SCANNER_OPTS="-Djava.io.tmpdir=/home/tmp" 

RUN set -x \
	&& mkdir /home/flatlib

COPY idle.sh /idle.sh
COPY system.properties /home/java-brp-cs-10/import2cg/system.properties

VOLUME /home
VOLUME /root

ENTRYPOINT ["/idle.sh"]






