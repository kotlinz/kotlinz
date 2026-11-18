plugins {
    kotlin("jvm") version "2.4.21"
    jacoco
    `maven-publish`
    id("pl.allegro.tech.build.axion-release") version "1.21.4"
    id("org.jetbrains.dokka") version "2.3.0"
}

scmVersion {
    tag {
        prefix.set("")
    }
}

group = "com.github.kotlinz.kotlinz"
version = scmVersion.version

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:6.2.7")
    testImplementation("io.kotest:kotest-assertions-core:6.2.7")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

dokka {
    moduleName.set("kotlinz")
    dokkaSourceSets.main {
        includes.from("Module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/kotlinz/kotlinz/tree/master/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }
    }
}
