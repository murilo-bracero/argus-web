FROM gradle:7.4.2-jdk11 as builder

WORKDIR /builder

ADD . /builder

RUN gradle build --stacktrace

FROM openjdk:11

ARG rest_port
ARG grpc_port
ARG app_version

WORKDIR /authapi

EXPOSE $rest_port $grpc_port

COPY --from=builder /builder/build/libs/auth-api-${app_version}.jar argus-web-auth-api.jar

ENTRYPOINT ["java", "-jar", "argus-web-auth-api.jar"]