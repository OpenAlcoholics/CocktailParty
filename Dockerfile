FROM openjdk:8-jdk-slim AS builder
WORKDIR /appSrc
COPY . .
RUN chmod +x gradlew && ./gradlew installDist -x test -x check

FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=builder /appSrc/build/install/CocktailParty .
EXPOSE 8080
CMD ./bin/CocktailParty
