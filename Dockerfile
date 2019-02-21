FROM openjdk:11-jdk-slim
MAINTAINER Mike Jackson <michael.jackson@digital.justice.gov.uk>

RUN apt-get update
RUN apt-get install -y curl
RUN apt-get install -y telnet

COPY build/libs/oasys-ndh-delius-replacement*.jar /root/oasys-ndh-delius-replacement.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/root/oasys-ndh-delius-replacement.jar"]
