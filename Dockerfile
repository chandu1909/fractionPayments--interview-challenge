FROM adoptopenjdk/openjdk11:alpine-jre
ADD build/libs/fraction-payments-0.0.1-SNAPSHOT-plain.jar paymentApp.jar
RUN mkdir /app
COPY build/libs/*.jar /app/paymentApp.jar
ENTRYPOINT ["java","-jar","/app/paymentApp.jar"]
EXPOSE 8090