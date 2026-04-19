// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register("detekt") {
    group = "verification"
    description = "Runs Detekt on the :app module."
    dependsOn(":app:detekt")
}