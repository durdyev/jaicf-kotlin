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
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/durdyev/kotlin-telegram-bot")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
                }
            }
        }
    }
}
