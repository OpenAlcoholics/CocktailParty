import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.2.61"
    id("org.jetbrains.dokka") version "0.9.17"

    id("org.flywaydb.flyway") version "5.1.4"
}

application {
    mainClassName = "group.openalcoholics.cocktailparty.HelloWorld"
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.slf4j:slf4j-simple:1.7.25")
    compile("com.google.guava:guava:26.0-jre")

    compile("io.github.cdimascio:java-dotenv:3.1.2")

    compile("io.vertx:vertx-web-api-contract:3.5.3")
    compile("io.vertx:vertx-lang-kotlin:3.5.3")
    compile("io.vertx:vertx-rx-java2:3.5.3")
    compile("io.reactivex.rxjava2:rxkotlin:2.2.0")
    compile("com.englishtown.vertx:vertx-guice:2.3.1")

    compile(group = "org.flywaydb", name = "flyway-core", version = "5.1.4")
    compile("org.jdbi:jdbi3-core:3.3.0")
    compile("org.jdbi:jdbi3-kotlin:3.3.0")
    compile("org.jdbi:jdbi3-postgres:3.3.0")
    compile("org.jdbi:jdbi3-kotlin-sqlobject:3.3.0")
    compile("com.zaxxer:HikariCP:3.2.0")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")

    testCompile(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.2.0")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.2.0")
    testCompile(kotlin("test-junit", "1.2.61"))
    testCompile(group = "name.falgout.jeffrey.testing.junit5", name = "guice-extension", version = "1.0.2")

}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("io.github.cdimascio:java-dotenv:3.1.2")
    }
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}
val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

val test by tasks.getting(org.gradle.api.tasks.testing.Test::class) {
    useJUnitPlatform()
}

val dokka by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
    // TODO maybe switch to javadoc (or another) format
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

flyway {
    val dotenv = dotenv {
        this.directory = "$rootDir/src/main/resources"
    }
    url = "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${dotenv["DB_NAME"]}"
    user = dotenv["DB_USER"]
    password = dotenv["DB_PASS"]
    locations = arrayOf("filesystem:src/main/resources/migration/sql")
}

repositories {
    jcenter()
}
