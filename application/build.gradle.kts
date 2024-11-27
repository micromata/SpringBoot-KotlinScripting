import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "1.9.10"
}

springBoot {
    mainClass.set("de.micromata.kotlinscripting.DemoApplication")
}

val kotlinVersion = "2.0.21"

val kotlinCompilerDependency = configurations.create("kotlinCompilerDependency")
val kotlinCompilerDependencies = mutableListOf<String>()
val versionOfSubmodules = "0.0.1"
// All submodules must be unpacked with direct usage in scripts:
val unpackSubmodules = listOf("business") // business must be unpacked, because KotlinScriptContext is used by scripts.

dependencies {
    unpackSubmodules.forEach { module ->
        implementation(project(":$module"))
    }
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Kotlin
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlin:kotlin-scripting-jsr223:$kotlinVersion")
    kotlinCompilerDependencies.add("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0")
    kotlinCompilerDependencies.forEach {
        kotlinCompilerDependency(it)
        implementation(it)
    }

    // Logging
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val kotlinCompilerDependencyFiles = kotlinCompilerDependency.map { it.name } + unpackSubmodules.map { "$it-$versionOfSubmodules.jar" }
tasks.named<BootJar>("bootJar") {
    // println(kotlinCompilerDependencyFiles.joinToString())
    exclude(kotlinCompilerDependencyFiles.map { "**/$it" })
}

tasks.withType<Jar> {
    unpackSubmodules.forEach { module ->
        dependsOn(":$module:jar")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().filter {
            // println("it.name=${it.name}")
            kotlinCompilerDependencyFiles.any { file -> it.name.contains(file) }
        }.map { if (it.isDirectory) it else zipTree(it) }
    })
}
