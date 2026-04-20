plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "2.3.0"
    application
}

group = "zov.deneg"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        allWarningsAsErrors.set(false)
        freeCompilerArgs.addAll(
            "-Xskip-prerelease-check",
            "-Xsuppress-version-warnings"
        )
    }
}

dependencies {
    implementation(libs.clickhouse.jdbc)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

tasks.named("startScripts") {
    dependsOn("processResources")
}

tasks.named("installDist") {
    dependsOn("build")
}
