FROM openjdk:13.0.1
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE
ADD target/smartsheet.test-0.0.1-SNAPSHOT app.jar
ENTRYPOINT ["java","-jar","/app.jar"]