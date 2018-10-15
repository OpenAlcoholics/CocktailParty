# CocktailParty ![CircleCI](https://img.shields.io/circleci/project/github/OpenAlcoholics/CocktailParty.svg) ![GitHub](https://img.shields.io/github/license/OpenAlcoholics/CocktailParty.svg) ![GitHub (pre-)release](https://img.shields.io/github/release/OpenAlcoholics/CocktailParty/all.svg)

Database backend for the [OpenCocktail.party](http://OpenCocktail.party) database.

## Compilation
Compile using [Gradle](https://gradle.org/) and Java 1.8:
```
./gradlew build
```

## Execution
The project may be executed with Gradle:
```
./gradlew run
```

or by running the following command, which builds the project and creates execution 
scripts for Windows and Linux at `build/install/CocktailParty/bin`:
```
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

## Generating a key-pair
To generate a key-pair for the JWT tokens, execute the following command:
```
keytool -genkey -keystore keystore.pfx -storetype pkcs12 -storepass secret -keyalg RSA -keysize 2048 -alias RS256 -keypass secret -sigalg SHA256withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
```

You can export the certificate from the keystore with:
```
keytool -exportcert -rfc -keystore .\keystore.pfx -alias RS256 > client.crt
```

Then you can extract the public key by:
```
openssl x509 -inform pem -in client.crt -pubkey -noout
```

The result, without the header and footer, can be used as your `publicKey` in the `auth` section
of the config file.
