plugins {
    kotlin("jvm") version "1.5.21"
    java
}

val groupIdDef: String by rootProject
val versionIdDef: String by rootProject

group = groupIdDef
version = versionIdDef

repositories {
    mavenCentral()
}

dependencies {
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}