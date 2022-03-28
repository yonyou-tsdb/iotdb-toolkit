FROM maven

COPY target/iotdb-ui-0.12.4-SNAPSHOT.jar iotdb-ui.jar

ENTRYPOINT ["java", "-jar", "iotdb-ui.jar"]
