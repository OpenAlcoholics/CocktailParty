FROM gradle:4.10-jdk8-slim AS builder
WORKDIR /appSrc
COPY . .
RUN gradle installDist -x test -x check

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=builder /appSrc/build/install/CocktailParty .
EXPOSE 8080
CMD ./bin/CocktailParty
