# Etapa 1: Construcción (Build)
# Usamos una imagen de Maven con Java 21 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el archivo pom.xml y descargamos dependencias (optimización de caché)
COPY pom.xml .
# Intentamos descargar dependencias (opcional, pero ayuda a la velocidad)
# RUN mvn dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Compilamos el proyecto saltando los tests para agilizar el despliegue
# El flag -DskipTests es vital en entornos CI/CD para evitar fallos por bases de datos no disponibles
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
# CORRECCIÓN: Usamos una imagen válida y ligera de Java 21
# '21-jre-alpine' es excelente porque ocupa muy poco espacio
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el archivo .jar generado en la etapa anterior
# Asegúrate de que el nombre coincida con el generado en target/ (basado en pom.xml)
# Usamos un wildcard *.jar para que no falle si cambia la versión
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto 8080 (puerto por defecto de Spring Boot)
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]