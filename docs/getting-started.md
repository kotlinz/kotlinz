# Getting started

Kotlinz is a typed functional programming library for Kotlin/JVM with no runtime dependencies. This page shows
how to add it to your build and walks through a first example.

## Installation

Kotlinz is published through [JitPack](https://jitpack.io/#kotlinz/kotlinz). Replace `$version` with the
latest release shown on the JitPack badge.

::: code-group

<!-- nocheck -->
```kotlin [Gradle (Kotlin DSL)]
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.kotlinz:kotlinz:$version")
}
```

```groovy [Gradle (Groovy DSL)]
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.kotlinz:kotlinz:$version"
}
```

```xml [Maven]
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

:::

Kotlinz targets JDK 17 or newer.

## A first example

Data types expose their most common operations as (mostly `infix`) members and companion functions, so you can
use them without touching type classes at all:

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()

val doubled = parse("21") fmap { it * 2 }
val sum = Maybe.liftA2 { a: Int, b: Int -> a + b }(parse("1"), parse("2"))
val chained = parse("8") bind { n -> if (n > 0) Maybe.Just(100 / n) else Maybe.None() }

check(doubled == Maybe.Just(42))
check(sum == Maybe.Just(3))
check(chained == Maybe.Just(12))
check(parse("x").getOrElse(0) == 0)
```

## Do-notation with `fx`

Chains of `bind` read better as a comprehension. `fx` is available on the companions of most monads:

```kotlin
import com.github.kotlinz.kotlinz.data.state.State

val nextId = State.fx<Int, String> {
  val id = State.get<Int>().bind()
  State.put(id + 1).bind()
  "user-$id"
}

check(nextId.run(7) == Pair("user-7", 8))
```

See [Monad comprehension](fx.md) for multi-shot monads and caveats.

## Writing generic code

Type-class instances are public interfaces (`MaybeFunctor`, `ListKFunctor`, ...). Generic code takes them as
parameters, works on `K1<F, A>`, and `narrow` recovers the concrete type:

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.type.Functor

fun <F> describe(functor: Functor<F>, v: K1<F, Int>): K1<F, String> = functor.fmap({ "#$it" }, v)

check(Maybe.narrow(describe(object : MaybeFunctor {}, Maybe.Just(1))) == Maybe.Just("#1"))
check(ListK.narrow(describe(object : ListKFunctor {}, listKOf(1, 2))) == listKOf("#1", "#2"))
```

## Next steps

- [Higher-kinded types and conventions](hkt.md) explains the `K1` encoding that everything else builds on.
- [Type classes](type-classes/README.md) covers the hierarchy and which data type implements which class.
- [Data types](data-types.md) tours `Maybe`, `Either`, `Validated`, `IO`, `State` and friends.
- The [guides overview](README.md) lists every guide in a suggested reading order.
