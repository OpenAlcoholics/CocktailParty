import com.jdiazcano.cfg4k.loaders.EnvironmentConfigLoader
import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.providers.OverrideConfigProvider
import com.jdiazcano.cfg4k.providers.getOrNull
import com.jdiazcano.cfg4k.sources.ClasspathConfigSource
import com.jdiazcano.cfg4k.sources.FileConfigSource
import com.jdiazcano.cfg4k.yaml.YamlConfigLoader
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version Version.kotlin
    id("org.jetbrains.dokka") version Version.dokka
    idea

    id("org.flywaydb.flyway") version Version.flyway
}

application {
    mainClassName = "group.openalcoholics.cocktailparty.Main"
}

dependencies {
    // Basics
    implementation(kotlin("stdlib-jdk8"))
    implementation(
        group = "io.github.microutils",
        name = "kotlin-logging",
        version = Version.kotlinLogging)
    implementation(group = "org.slf4j", name = "slf4j-simple", version = Version.slf4j)
    implementation(group = "com.google.guava", name = "guava", version = Version.guava)

    // Config
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-core", version = Version.cfg4k)
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-yaml", version = Version.cfg4k)

    // Vertx
    implementation(group = "io.vertx", name = "vertx-web-api-contract", version = Version.vertx)
    implementation(group = "io.vertx", name = "vertx-lang-kotlin", version = Version.vertx) {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation(group = "io.vertx", name = "vertx-auth-jwt", version = Version.vertx)
    implementation(group = "com.englishtown.vertx", name = "vertx-guice",
        version = Version.vertxGuice)

    // Database
    implementation(group = "org.flywaydb", name = "flyway-core", version = Version.flyway)
    implementation(group = "org.jdbi", name = "jdbi3-core", version = Version.jdbi)
    implementation(group = "org.jdbi", name = "jdbi3-kotlin", version = Version.jdbi)
    implementation(group = "org.jdbi", name = "jdbi3-postgres", version = Version.jdbi)
    implementation(group = "org.jdbi", name = "jdbi3-kotlin-sqlobject", version = Version.jdbi)
    implementation(group = "com.zaxxer", name = "HikariCP", version = Version.hikariCp)
    implementation(
        group = "com.fasterxml.jackson.module",
        name = "jackson-module-kotlin",
        version = Version.jackson)

    // Testing
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-api",
        version = Version.junit)
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = Version.junit)
    testImplementation(kotlin("test-junit5"))
    testImplementation(
        group = "name.falgout.jeffrey.testing.junit5",
        name = "guice-extension",
        version = Version.junitGuice)
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath(group = "com.jdiazcano.cfg4k", name = "cfg4k-core", version = Version.cfg4k)
        classpath(group = "com.jdiazcano.cfg4k", name = "cfg4k-yaml", version = Version.cfg4k)
        classpath(group = "org.postgresql", name = "postgresql", version = Version.postgresql)
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

fun configProvider(): ConfigProvider {
    val envLoader = EnvironmentConfigLoader()
    val envProvider = DefaultConfigProvider(envLoader)

    val file = File("config.yaml")
    val fileProvider = if (!file.isFile) {
        null
    } else {
        val fileSource = FileConfigSource(file)
        val fileLoader = YamlConfigLoader(fileSource)
        DefaultConfigProvider(fileLoader)
    }

    val defaultFile = File("$rootDir/src/main/resources/defaultConfig.properties")
    val defaultSource = FileConfigSource(defaultFile)
    val defaultLoader = PropertyConfigLoader(defaultSource)
    val defaultProvider = DefaultConfigProvider(defaultLoader)

    return if (fileProvider == null) {
        OverrideConfigProvider(envProvider, defaultProvider)
    } else {
        OverrideConfigProvider(envProvider, fileProvider, defaultProvider)
    }
}

flyway {
    val config = configProvider()
    val host = config.getOrNull<String>("database.host")
    val port = config.getOrNull<Int>("database.port")
    val name = config.getOrNull<String>("database.name")
    val user = config.getOrNull<String>("database.user")
    val pass = config.getOrNull<String>("database.pass")
    if (host == null
        || port == null
        || name == null
        || user == null
        || pass == null) {
        System.err.println("database unconfigured")
    } else {
        url = "jdbc:postgresql://$host:$port/$name"
        this.user = user
        password = pass
        locations = arrayOf("filesystem:src/main/resources/openapi/sql")
    }
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

repositories {
    jcenter()
}
