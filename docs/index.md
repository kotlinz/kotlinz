---
layout: home

hero:
  name: Kotlinz
  text: Functional programming for Kotlin
  tagline: HKT-encoded type classes, data types, and category-theory constructs, with no dependencies.
  image:
    src: /logo.svg
    alt: Kotlinz
  actions:
    - theme: brand
      text: Get Started
      link: /getting-started
    - theme: alt
      text: API Reference
      link: /api/
      target: _self
    - theme: alt
      text: GitHub
      link: https://github.com/kotlinz/kotlinz

features:
  - icon: 🧩
    title: Type classes
    details: Functor, Applicative, Monad, Traversable, MTL-style classes, Profunctor, Arrow and more, as ordinary interfaces over the lightweight K1 encoding.
    link: /type-classes/
    linkText: Explore the hierarchy
  - icon: 📦
    title: Data types
    details: Maybe, Either, Try, Validated, Ior, Eval, IO, Reader, Writer, State, RWS, Cont, ListK, NonEmptyList and functor combinators.
    link: /data-types
    linkText: Browse data types
  - icon: 🥞
    title: Monad transformers
    details: MaybeT, EitherT, ReaderT, StateT and WriterT stack effects on top of any monad, with matching MTL instances.
    link: /transformers
    linkText: Stack effects
  - icon: ✨
    title: fx comprehension
    details: Haskell do-notation and Scala for-comprehensions, built on Kotlin coroutines, available on the companions of most monads.
    link: /fx
    linkText: Write fx blocks
  - icon: 🔭
    title: Comonads
    details: Store, Env, Traced, Cofree and Density for context-dependent computations, alongside the Comonad type class.
    link: /comonads
    linkText: Meet the comonads
  - icon: 🧮
    title: Category theory
    details: Natural transformations, adjunctions, Yoneda, Codensity, Kan extensions, Day convolution, Free / FreeAp and recursion schemes.
    link: /category-theory
    linkText: Go deeper
---

## Install

Kotlinz is published on [JitPack](https://jitpack.io/#kotlinz/kotlinz):

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.kotlinz:kotlinz:$version")
}
```

## A taste of Kotlinz

Data types come with familiar `infix` operators, and `fx` gives you do-notation for any monad:

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()

val doubled = parse("21") fmap { it * 2 }
val sum = Maybe.fx {
  val a = parse("1").bind()
  val b = parse("2").bind()
  a + b
}

check(doubled == Maybe.Just(42))
check(sum == Maybe.Just(3))
check(parse("x").getOrElse(0) == 0)
```

Continue with [Getting Started](./getting-started.md), or jump straight into the [guides](./README.md).
