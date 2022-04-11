FROM maven

COPY target/iotdb-ui-0.12.4.jar iotdb-ui.jar

COPY application.yml application.yml

ENTRYPOINT java -jar -Dspring-boot.run.jvmArguments="-Dspring.config.location=application.yml" iotdb-ui.jar
