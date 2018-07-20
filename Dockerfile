FROM openjdk:8-jdk-alpine
MAINTAINER Mike Jackson <michael.jackson@digital.justice.gov.uk>

COPY build/libs/oasys-ndh-delius-replacement*.jar /root/oasys-ndh-delius-replacement.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/root/oasys-ndh-delius-replacement.jar"]
