plugins {
    id("java-library")
}

val kotlinVersion: String by rootProject.extra

dependencies {
    implementation(project(":commons"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
