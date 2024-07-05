import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Core component"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Core component. Provides DSL, Tests API and multiple implementable interfaces."

plugins {
    `jaicf-kotlin`
    `maven-publish`
//    `jaicf-junit`
    `java-test-fixtures`
}

dependencies {
    api(slf4j("slf4j-api"))

    implementation(`tomcat-servlet`())
    implementation(ktor("ktor-server-core"))
    implementation("org.junit.jupiter:junit-jupiter-api" version { jUnit })
    implementation("org.junit.jupiter:junit-jupiter-params" version { jUnit })

    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("test"))
    testImplementation("ch.qos.logback:logback-classic:1.2.3")

    testFixturesApi("org.junit.jupiter:junit-jupiter-api" version { jUnit })
    testFixturesApi(kotlin("test-junit"))
    testFixturesApi(kotlin("test"))
    testFixturesApi("ch.qos.logback:logback-classic:1.2.3")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/durdyev/jaicf-kotlin")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}