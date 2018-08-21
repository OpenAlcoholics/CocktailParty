# CocktailParty ![CircleCI](https://img.shields.io/circleci/project/github/OpenAlcoholics/CocktailParty.svg) ![GitHub](https://img.shields.io/github/license/OpenAlcoholics/CocktailParty.svg) ![GitHub (pre-)release](https://img.shields.io/github/release/OpenAlcoholics/CocktailParty/all.svg)

Database backend for the [OpenCocktail.party](http://OpenCocktail.party) database.

## Compilation
Compile using [Gradle](https://gradle.org/) and Java 1.8:
```
./gradlew build
```

## Execution
You may simply run the project with Gradle:
```
./gradlew run
```

or you can run the following command, which builds the project and creates execution 
scripts for Windows and Linux at `build/install/CocktailParty/bin`:
```
./gradlew installDist
```
