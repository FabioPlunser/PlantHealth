# Use the official Gradle image as a build environment
FROM gradle:8.1.1-jdk17
WORKDIR /app
COPY . /app
ENV SPRING_PROFILES_ACTIVE=deploy
#RUN #gradle clean bootRun --no- \
CMD ["gradle", "clean", "bootRun", "--no-daemon"]

## Use a lightweight OpenJDK image as a base image
#FROM openjdk:17-jdk-slim
#
## Copy the built JAR file from the build environment to the new image
#COPY --from=build /app/build/libs/*.jar /app/app.jar
#
## Expose the port that the application listens on
#EXPOSE 8080
#
#ENV SPRING_PROFILES_ACTIVE=prod
##
## Run the application
#CMD ["java", "-jar", "/app/app.jar"]
