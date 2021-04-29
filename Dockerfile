FROM gradle:jdk11 AS BUILDER

COPY . /tmp/src

WORKDIR /tmp/src

RUN mkdir -p ~/.gradle && echo -n "org.gradle.daemon=false\norg.gradle.vfs.watch=false" >> ~/.gradle/gradle.properties

RUN echo -n 'task("copyRuntimeLibs", Copy::class) { from(configurations.default).into("$buildDir/libs") }' >> build.gradle.kts

RUN gradle build copyRuntimeLibs

FROM debian:latest

RUN apt update && apt install -y default-jre && rm -rf /var/lib/apt/lists && adduser signal && chmod 777 /tmp

COPY --from=builder /tmp/src/build/scripts/signal-cli /opt/bin/
COPY --from=builder /tmp/src/build/libs/*.jar /opt/lib/

USER signal
WORKDIR /home/signal

ENTRYPOINT ["/opt/bin/signal-cli"]
