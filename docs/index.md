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
    details: Functor, Applicative, Selective, Monad, Traversable, Filterable, Align, MTL-style classes, Profunctor, Arrow and more, as ordinary interfaces over the lightweight K1 encoding.
    link: /type-classes/
    linkText: Explore the hierarchy
  - icon: 📦
    title: Data types
    details: Maybe, Either, Try, Validated, Ior, Eval, IO, Reader, Writer, State, RWS, Cont, Select, ListK, NonEmptyList and functor combinators, with Eq, Order, Show and Hash instances.
    link: /data-types
    linkText: Browse data types
  - icon: 🌳
    title: Data structures
    details: LazyList, Chain, Tree, NonEmptySet, NonEmptyMap and ListZipper, with Foldable1, Traversable1, Align and Witherable instances.
    link: /data-structures
    linkText: Pick a structure
  - icon: ⚡
    title: Effects
    details: IO with Resource and Ref, a position-aware Parser, a pure random Gen and backtracking Logic.
    link: /effects
    linkText: Run effects
  - icon: 🥞
    title: Monad transformers
    details: Fifteen transformers, from MaybeT, EitherT and StateT to RWST, ContT, ListT, LogicT and FreeT, with lifted MTL instances, MonadTrans and MFunctor.
    link: /transformers
    linkText: Stack effects
  - icon: 🔍
    title: Optics
    details: Profunctor-encoded Iso, Lens, Prism, Optional and Traversal, plus Getter, Fold and Setter, all composable with andThen.
    link: /optics
    linkText: Focus on data
  - icon: ✨
    title: fx comprehension
    details: Haskell do-notation and Scala for-comprehensions, built on Kotlin coroutines, available on the companions of most monads.
    link: /fx
    linkText: Write fx blocks
  - icon: 🔭
    title: Comonads and automata
    details: Store, Env, Traced, Cofree and their transformers, plus Moore and Mealy machines and composable folds.
    link: /comonads
    linkText: Meet the comonads
  - icon: 🧮
    title: Category theory
    details: Natural transformations, adjunctions, Yoneda, Kan extensions, Day convolution, Free / FreeAp / Freer / FreeArrow and recursion schemes over Fix, Mu and Nu.
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
