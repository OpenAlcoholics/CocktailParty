# CocktailParty [![CircleCI branch](https://img.shields.io/circleci/project/github/OpenAlcoholics/CocktailParty/develop.svg)](https://github.com/OpenAlcoholics/CocktailParty) ![GitHub](https://img.shields.io/github/license/OpenAlcoholics/CocktailParty.svg) [![GitHub (pre-)release](https://img.shields.io/github/release/OpenAlcoholics/CocktailParty/all.svg)](https://github.com/OpenAlcoholics/CocktailParty/releases)

Database backend for the [OpenCocktail.party](http://OpenCocktail.party) database.

The implemented API is specified with [OpenAPI](https://www.openapis.org/) in
[the CockBot-OpenAPI repository](../CockBot-OpenAPI/OpenCocktail.yaml).
It is served at [API.OpenCocktail.party](https://api.OpenCocktail.party),
but keep in mind that no stable version (1.0+) has been released yet.

## Compilation

Compile using [Gradle](https://gradle.org/) and Java 1.8:

```bash
./gradlew build
```

## Execution

The project may be executed with Gradle:

```bash
./gradlew run
```

or by running the following command, which builds the project and creates execution
scripts for Windows and Linux at `build/install/CocktailParty/bin`:

```bash
./gradlew installDist
```

## Configuration

The backend can be configured by [environment variables](#environment-variables) or
a [config file](#config-file). Environment variables override values from the config file.

### Config file

The `config.yaml` file should reside in the working directory.

A commented example config file can be found [in the repository root](config.example.yaml).

### Environment variables

The values from the config file can be overridden by environment variables.

As for the format, the environment variable equivalent of the `config.yaml`

```yaml
database:
  host: localhost
```

is `DATABASE_HOST=localhost`.

### Key generation cheat sheet

```bash
openssl genrsa -out key.pem 2048
# Copy the publicKey from the resulting pub.pem
openssl rsa -in .\key.pem -outform PEM -pubout -out pub.pem
# Copy the private key from the resulting private.pem
openssl pkcs8 -topk8 -nocrypt -in .\key.pem -out private.pem
```
