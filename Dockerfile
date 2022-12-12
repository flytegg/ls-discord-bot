FROM gradle:7.5.1-jdk17 AS BUILD
COPY --chown=gradle:gradle . /home/gradle/
RUN cd /home/gradle/
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:17
RUN mkdir /app
COPY --from=build /home/gradle/build/libs/*-all.jar /app/bot.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/bot.jar"]