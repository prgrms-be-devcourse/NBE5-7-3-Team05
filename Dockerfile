FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

RUN curl -o wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && \
    chmod +x wait-for-it.sh

ENTRYPOINT ["./wait-for-it.sh", "db:3306", "--", "java", "-Dspring.profiles.active=deploy", "-jar", "app.jar"]