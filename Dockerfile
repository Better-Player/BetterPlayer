#Builder docker container
FROM openjdk:11.0.10-jdk as builder
RUN mkdir -p /usr/src/betterplayer
COPY . /usr/src/betterplayer

WORKDIR /usr/src/betterplayer
RUN chmod +x gradlew

RUN ./gradlew shadowjar


#Runtime Docker container
FROM openjdk:11.0.10-jre
ENV IS_DOCKER=true

RUN mkdir -p /app/

COPY --from=builder /usr/src/betterplayer/build/libs/*.jar /app/betterplayer.jar
COPY ./entrypoint.sh /app/entrypoint.sh

ENTRYPOINT [ "/app/entrypoint.sh" ]