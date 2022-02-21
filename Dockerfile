FROM maven

COPY target/iotdb-ui-0.12.3-SNAPSHOT.jar iotdb-ui-0.12.3-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "iotdb-ui-0.12.3-SNAPSHOT.jar"]
