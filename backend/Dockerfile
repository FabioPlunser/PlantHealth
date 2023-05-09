FROM eclipse-temurin:17

# TODO: Get current Version instead of fixed Version
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", "-jar", "/app.jar"]

