plugins {
    kotlin("jvm")
    java
    `java-library`
    `maven-publish`
}

val groupIdDef: String by rootProject
val versionIdDef: String by rootProject
val kspVersion: String by rootProject


group = groupIdDef
version = versionIdDef


val artifactIdDef = "js-array-ksp"

repositories {
    mavenCentral()
    flatDir {
        dir("libs")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation("com.google.devtools.ksp:$kspVersion")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation(files("libs/formatter.jar"))

    implementation(project(":js-array-core"))

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