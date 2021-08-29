plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.5.21-1.0.0-beta07" apply true
    java
    `java-library`
    `maven-publish`
}

val groupIdDef: String by rootProject
val versionIdDef: String by rootProject

group = groupIdDef
version = versionIdDef
val artifactIdDef = "js-array-basic"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation(project(":js-array-core"))
    implementation(project(":js-array-ksp"))
    ksp(project(":js-array-ksp"))
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = groupIdDef
            version = versionIdDef
            artifactId = artifactIdDef
            from(components["kotlin"])
        }
    }
}