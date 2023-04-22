FROM gradle:8.1.1-jdk17 AS BUILD
COPY --chown=gradle:gradle . /home/gradle/
RUN cd /home/gradle/
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:17
RUN mkdir /app
COPY --from=build /home/gradle/build/libs/*-all.jar /app/bot.jar
RUN cd /app
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom", "-Duser.dir=/app/", "-jar","/app/bot.jar"]