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
    kotlin("jvm") version "1.2.70"
    id("org.jetbrains.dokka") version "0.9.17"
    idea

    id("org.flywaydb.flyway") version "5.1.4"
}

application {
    mainClassName = "group.openalcoholics.cocktailparty.Main"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.microutils:kotlin-logging:1.6.10")
    implementation("org.slf4j:slf4j-simple:1.7.25")
    implementation("com.google.guava:guava:26.0-jre")

    implementation("com.jdiazcano.cfg4k:cfg4k-core:0.9.0")
    implementation("com.jdiazcano.cfg4k:cfg4k-yaml:0.9.0")

    implementation("io.vertx:vertx-web-api-contract:3.5.3")
    implementation("io.vertx:vertx-lang-kotlin:3.5.3") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("io.vertx:vertx-auth-jwt:3.5.3")
    implementation("com.englishtown.vertx:vertx-guice:2.3.1")

    implementation(group = "org.flywaydb", name = "flyway-core", version = "5.1.4")
    implementation("org.jdbi:jdbi3-core:3.3.0")
    implementation("org.jdbi:jdbi3-kotlin:3.3.0")
    implementation("org.jdbi:jdbi3-postgres:3.3.0")
    implementation("org.jdbi:jdbi3-kotlin-sqlobject:3.3.0")
    implementation("com.zaxxer:HikariCP:3.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.3.1")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.3.1")
    testImplementation(kotlin("test-junit5", "1.2.70"))
    testImplementation(group = "name.falgout.jeffrey.testing.junit5", name = "guice-extension", version = "1.0.2")
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.jdiazcano.cfg4k:cfg4k-core:0.9.0")
        classpath("com.jdiazcano.cfg4k:cfg4k-yaml:0.9.0")
        classpath("org.postgresql:postgresql:42.2.5")
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
