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
    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("com.konghq:unirest-java:3.14.5")
    implementation("org.jsoup:jsoup:1.17.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}