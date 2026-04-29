import io.gitlab.arturbosch.detekt.Detekt
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.zovdeneg.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    val localProperties = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) {
            f.inputStream().use { load(it) }
        }
    }

    defaultConfig {
        applicationId = "com.zovdeneg.app"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val useMockHttp =
            localProperties.getProperty("zov.useMockHttp")?.trim()?.equals("true", ignoreCase = true) == true
        val isBiometryEnabled =
            localProperties.getProperty("zov.isBiometryEnabled")?.trim()?.equals("true", ignoreCase = true) == true
        val userApiBase =
            if (useMockHttp) {
                "https://api.zovdeneg.mock"
            } else {
                localProperties.getProperty("zov.userApiBaseUrl")?.trim()?.trimEnd('/')
                    ?: "http://10.0.2.2:8080"
            }
        val securitiesApiBase =
            if (useMockHttp) {
                "https://api.zovdeneg.mock"
            } else {
                localProperties.getProperty("zov.securitiesApiBaseUrl")?.trim()?.trimEnd('/')
                    ?: "http://10.0.2.2:8081"
            }

        buildConfigField("String", "API_BASE_URL", "\"$userApiBase/\"")
        buildConfigField("String", "SECURITIES_API_BASE_URL", "\"$securitiesApiBase/\"")
        buildConfigField("boolean", "USE_MOCK_HTTP_ENGINE", if (useMockHttp) "true" else "false")
        buildConfigField("boolean", "IS_BIOMETRY_AVAILABLE", if (isBiometryEnabled) "false" else "true")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "API_BASE_URL", "\"https://api.zovdengi.ru/v1/\"")
            buildConfigField("String", "SECURITIES_API_BASE_URL", "\"https://api.zovdengi.ru/v1/\"")
            buildConfigField("boolean", "USE_MOCK_HTTP_ENGINE", "false")
            buildConfigField("boolean", "IS_BIOMETRY_AVAILABLE", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.charty)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.mock)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.security.crypto)
    ksp(libs.hilt.compiler)
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(rootProject.layout.projectDirectory.file("detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "11"
    reports {
        html.required.set(true)
        html.outputLocation.set(file("build/reports/detekt/detekt.html"))
        txt.required.set(true)
        txt.outputLocation.set(file("build/reports/detekt/detekt.txt"))
        xml.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

tasks.named("check") {
    dependsOn(tasks.named("detekt"))
}
