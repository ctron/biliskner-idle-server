FROM java:8-jre

MAINTAINER jreimann@redhat.com

COPY target/idle-test-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar .

EXPOSE 4242

CMD ["java", "-jar", "idle-test-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]