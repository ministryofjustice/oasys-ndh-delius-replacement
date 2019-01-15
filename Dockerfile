FROM openjdk:11-jdk-slim
MAINTAINER Mike Jackson <michael.jackson@digital.justice.gov.uk>

COPY build/libs/oasys-ndh-delius-replacement*.jar /root/oasys-ndh-delius-replacement.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/root/oasys-ndh-delius-replacement.jar"]
