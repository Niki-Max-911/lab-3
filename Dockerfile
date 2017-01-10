FROM driv/docker-maven-java-oracle

ARG PROJECT_DIR="/opt/lab4"

WORKDIR $PROJECT_DIR

COPY ./src ./src/
COPY ./pom.xml ./

RUN mvn package

CMD ["java","-jar","./target/InstagramPhotoClonner.jar"]
