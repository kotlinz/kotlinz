plugins {
    kotlin("jvm") version "2.4.21"
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
