plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
    application
    jacoco
    id("checkstyle")
    id("io.freefair.lombok") version "8.4"
}

application {
    mainClass.set("hexlet.code.App")
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:5.6.3")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("io.javalin:javalin-bundle:5.6.3")
    implementation("io.javalin:javalin-rendering:5.6.2")
    implementation("gg.jte:jte:3.1.5")

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}