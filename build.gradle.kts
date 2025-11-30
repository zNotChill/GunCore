plugins {
    kotlin("jvm") version "2.3.0-dev-9673"
}

group = "me.znotchill"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven("https://redirector.kotlinlang.org/maven/bootstrap")
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
        content {
            includeModule("net.minestom", "minestom")
            includeModule("net.minestom", "testing")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.minestom:minestom:2025.10.18-1.21.10")
    implementation("me.znotchill:blossom:1.4.7")
    implementation("io.github.xn32:json5k:0.3.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("net.kyori:adventure-text-minimessage:4.24.0")
    implementation("net.kyori:adventure-api:4.24.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("me.znotchill.marmot:minestom-api:1.2.17")
    implementation("me.znotchill.marmot:common:1.2.17")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}