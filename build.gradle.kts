plugins {
    kotlin("jvm") version "2.4.21"
    jacoco
    id("pl.allegro.tech.build.axion-release") version "1.21.4"
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
