FROM gradle:7.4.2-jdk17 as builder

WORKDIR /builder

ADD . /builder

RUN gradle build

FROM openjdk:17-alpine

ARG rest_port
ARG grpc_port

WORKDIR /authapi

EXPOSE $rest_port $grpc_port

COPY --from=builder /builder/build/libs/CustomerBackend.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]