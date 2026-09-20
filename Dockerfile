FROM eclipse-temurin:8-jdk-jammy

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

RUN apt-get update && apt-get install -y ant && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN ant -f build.xml jar

CMD ["java", "-cp", "dist/Project.jar", "Presentation.Program"]