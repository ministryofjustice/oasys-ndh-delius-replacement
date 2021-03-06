# oasys-delius-data-shovel
Tactical Micro-service to replace NDH OASys - NDelius link

## Continuous Integration
https://circleci.com/gh/noms-digital-studio/thats-not-my-delius

## Gradle commands

### Build and run tests
```
./gradlew build
```

### Assessmble the app
```
./gradlew assemble
```

This makes the JAR executable by including a manifest. 

### Start the application dev profile
This profile starts the application additional configuration this mode uses an in memory H2 (empty) database and is
populated with a sample data set

```
SPRING_PROFILES_ACTIVE=dev java -jar build/libs/new-nomis-api.jar
```

### Start the application with Nomis Oracle db
```
SPRING_PROFILES_ACTIVE=oracle SPRING_DATASOURCE_URL=jdbc:oracle:thin:@<VM Oracle IP address>:1521:<sid> SPRING_DATASOURCE_USERNAME=myuser SPRING_DATASOURCE_PASSWORD=mypassword -jar build/libs/new-nomis-api.jar
```

### Start the application with the OASys SOAP links to NDH
IMPORTANT: This profile is disabled by default. Make active profile OASYS_LINKS to enable the inbound SOAP endpoints.  
Without this profile enabled, the application SOAP endpoints will not be active.

### Start the application with the XTAG event feed into OASys
IMPORTANT: This profile is disabled by default. Make active profile XTAG_FEED to enable the event feed into OASys  
Without this profile enabled, the XTAG event feed from NOMIS into OASys will not be active.

### Additional configuration
The application is configured with conventional Spring parameters.
The Spring documentation can be found here:

https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html

### Default port
Starts the application on port '8080'.
To override, set server.port (eg SERVER_PORT=8099 java -jar etc etc)

## Documentation
http://localhost:8080/api/swagger-ui.html

## Endpoints curl examples

### Trigger OASys extract
```
curl -X POST http://localhost:8080/oasysExtract
```

### Check queue status
```
curl http://localhost:8080/activemq/queues/OASYS_MESSAGES

```

### Application info
```
curl -X GET http://localhost:8080/info
```

### Application health  
```
curl -X GET http://localhost:8080/health
```


