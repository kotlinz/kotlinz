# Kotlinz

[![](https://jitpack.io/v/kotlinz/kotlinz.svg)](https://jitpack.io/#kotlinz/kotlinz)
[![CI](https://github.com/kotlinz/kotlinz/actions/workflows/ci.yml/badge.svg)](https://github.com/kotlinz/kotlinz/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/kotlinz/kotlinz/branch/master/graph/badge.svg)](https://codecov.io/gh/kotlinz/kotlinz)

Kotlinz is a library for functional programming.

## Dependency

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.kotlinz:kotlinz:$version")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.kotlinz:kotlinz:$version"
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.kotlinz</groupId>
    <artifactId>kotlinz</artifactId>
    <version>$version</version>
</dependency>
```

## Related
- [Scalaz](https://github.com/scalaz/scalaz)
- [Swiftz](https://github.com/typelift/Swiftz)
- [highj](https://github.com/DanielGronau/highj)

## License
see [LICENSE](LICENSE)

## Author

[@unhappychoice](https://unhappychoice.com)
