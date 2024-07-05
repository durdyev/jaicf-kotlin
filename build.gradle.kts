plugins {
    `jaicf-github-release`
    `maven-publish`
}

allprojects {
    group = "pro.ninjacoder.justai.jaicf"
    version = "1.3.8-SNAPSHOT"

    repositories {
        mavenCentral()
        maven(uri("https://jitpack.io"))
        google()
    }
}
