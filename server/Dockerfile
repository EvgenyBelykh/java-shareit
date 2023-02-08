#FROM amazoncorretto:11-alpine
#COPY target/*.jar shareit-server.jar
#ENTRYPOINT ["java","-jar","/shareit-server.jar"]

FROM amazoncorretto:11-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]