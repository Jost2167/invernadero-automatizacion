# Ejecutar desde la raíz del proyecto:
#   docker build -f backend/Dockerfile -t invernadero-backend .

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# El pom.xml raíz declara módulos en tools — Maven necesita sus pom.xml
# para validar la estructura aunque no lo compilemos
COPY pom.xml ./pom.xml
COPY backend/pom.xml ./backend/pom.xml
COPY tools/codegen/pom.xml ./tools/codegen/pom.xml
COPY tools/taiga-sync/pom.xml ./tools/taiga-sync/pom.xml
RUN mvn -pl backend dependency:go-offline -q

COPY backend/src ./backend/src
RUN mvn -pl backend package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/backend/target/invernadero-automatizacion-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
