plugins {
    id("java-library")
    kotlin("jvm") version "2.0.21"
}

val kotlinVersion: String by rootProject.extra

dependencies {
    implementation(project(":misc"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
