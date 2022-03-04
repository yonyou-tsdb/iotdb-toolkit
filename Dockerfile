FROM maven

COPY target/iotdb-ui-0.12.3.jar iotdb-ui.jar

ENTRYPOINT ["java", "-jar", "iotdb-ui.jar"]
