#Builder docker container
FROM gradle:7.2-jdk16 AS BUILDER
COPY ./src /usr/src/betterplayer/src
COPY build.gradle /usr/src/betterplayer
COPY settings.gradle /usr/src/betterplayer

WORKDIR /usr/src/betterplayer

RUN gradle releasejar


FROM ubuntu:focal
RUN apt update && apt install --no-install-recommends -y ca-certificates openjdk-16-jre-headless
COPY --from=BUILDER /usr/src/betterplayer/releases/BetterPlayer-*-RELEASE.jar /usr/jar/betterplayer.jar

ENTRYPOINT [ "java", "-jar", "/usr/jar/betterplayer.jar" ]
