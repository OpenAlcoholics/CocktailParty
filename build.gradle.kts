plugins {
    application
    kotlin("jvm") version "1.2.61"
    id("org.jetbrains.dokka") version "0.9.17"
}

application {
    mainClassName = "group.openalcoholics.cocktailparty.HelloWorld"
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.google.guava:guava:26.0-jre")
    testCompile("junit:junit:4.12")
}

val dokka by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
    // TODO maybe switch to javadoc (or another) format
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

repositories {
    jcenter()
}
