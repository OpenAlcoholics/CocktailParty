import com.jdiazcano.cfg4k.loaders.EnvironmentConfigLoader
import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.providers.OverrideConfigProvider
import com.jdiazcano.cfg4k.providers.getOrNull
import com.jdiazcano.cfg4k.sources.FileConfigSource
import com.jdiazcano.cfg4k.yaml.YamlConfigLoader
import org.flywaydb.gradle.task.FlywayMigrateTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version Plugin.KOTLIN
    id("org.jetbrains.dokka") version Plugin.DOKKA
    idea

    id("org.flywaydb.flyway") version Plugin.FLYWAY

    id("com.github.ben-manes.versions") version Plugin.VERSIONS
}

application {
    mainClassName = "group.openalcoholics.cocktailparty.Main"
}

version = "0.2.0"

dependencies {
    // Basics
    implementation(kotlin("stdlib-jdk8"))
    implementation(
        group = "io.github.microutils",
        name = "kotlin-logging",
        version = Lib.KOTLIN_LOGGING)
    implementation(group = "org.slf4j", name = "jul-to-slf4j", version = Lib.SLF4J)
    implementation(group = "org.slf4j", name = "slf4j-simple", version = Lib.SLF4J)
    implementation(group = "com.google.guava", name = "guava", version = Lib.GUAVA)
    implementation(group = "com.google.inject", name = "guice", version = Lib.GUICE)

    // Config
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-core", version = Lib.CFG4K)
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-yaml", version = Lib.CFG4K)
    // Needed by CFG4K, explicitly depending on it to include the correct version
    implementation(kotlin("reflect", Lib.KOTLIN))

    // Vertx
    implementation(group = "io.vertx", name = "vertx-web-api-contract", version = Lib.VERTX)
    implementation(group = "io.vertx", name = "vertx-lang-kotlin", version = Lib.VERTX) {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation(group = "io.vertx", name = "vertx-auth-jwt", version = Lib.VERTX)
    implementation(
        group = "com.englishtown.vertx",
        name = "vertx-guice",
        version = Lib.VERTX_GUICE)

    // Database
    implementation(group = "org.flywaydb", name = "flyway-core", version = Lib.FLYWAY)
    implementation(group = "org.jdbi", name = "jdbi3-core", version = Lib.JDBI)
    implementation(group = "org.jdbi", name = "jdbi3-kotlin", version = Lib.JDBI)
    implementation(group = "org.jdbi", name = "jdbi3-postgres", version = Lib.JDBI)
    implementation(group = "org.jdbi", name = "jdbi3-kotlin-sqlobject", version = Lib.JDBI)
    implementation(group = "com.zaxxer", name = "HikariCP", version = Lib.HIKARI_CP)
    implementation(
        group = "com.fasterxml.jackson.module",
        name = "jackson-module-kotlin",
        version = Lib.JACKSON)

    // Testing
    testRuntime(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = Lib.JUNIT)
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-api",
        version = Lib.JUNIT)
    testImplementation(
        group = "name.falgout.jeffrey.testing.junit5",
        name = "guice-extension",
        version = Lib.JUNIT_GUICE)
    testImplementation(group = "io.mockk", name = "mockk", version = Lib.MOCK_K)
    testImplementation(group = "org.assertj", name = "assertj-core", version = Lib.ASSERT_J)
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath(group = "com.jdiazcano.cfg4k", name = "cfg4k-core", version = Lib.CFG4K)
        classpath(group = "com.jdiazcano.cfg4k", name = "cfg4k-yaml", version = Lib.CFG4K)
        classpath(group = "org.postgresql", name = "postgresql", version = Lib.POSTGRESQL)
    }
}

tasks {
    "processResources"(ProcessResources::class) {
        filesMatching("**/version.properties") {
            filter {
                it.replace("%APP_VERSION%", version.toString())
            }
        }
    }

    withType(KotlinCompile::class) {
        kotlinOptions.jvmTarget = "1.8"
    }

    "test"(Test::class) {
        useJUnitPlatform()
    }

    "dokka"(DokkaTask::class) {
        // TODO maybe switch to javadoc (or another) format
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }

    create<FlywayMigrateTask>("flywayMigrateWithMock") {
        locations = arrayOf(
            "filesystem:src/main/resources/openapi/sql",
            "filesystem:src/main/resources/openapi/mockSql"
        )
    }
}

/**
 * Gets a [ConfigProvider] instance for executing Flyway migrations with Gradle.
 */
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
