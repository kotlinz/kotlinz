package com.github.kotlinz.kotlinz.data.freeap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * The free applicative over an arbitrary type constructor `F`.
 *
 * Effects form a static list, so programs can be inspected without running them
 * (e.g. collecting required config keys with [analyze]) and later interpreted into any
 * [Applicative] with [foldMap]. Prefer it over [com.github.kotlinz.kotlinz.data.free.Free]
 * when later instructions never depend on earlier results.
 *
 * Instances: [FreeApFunctor], [FreeApApplicative].
 *
 * ```kotlin
 * fun <A> read(key: String, parse: (String) -> A): FreeAp<Read.T, A> = FreeAp.lift(Read(key, parse))
 *
 * val user = read("name") { it } ap (read("age") { it.toInt() } fmap { age -> { name: String -> "$name:$age" } })
 * user.analyze(SumMonoid::class.java, count).value // 2 instructions, nothing executed
 * Identity.narrow(user.foldMap(config, object: IdentityMonad {})).value
 * ```
 */
sealed class FreeAp<F, A>: K2<FreeAp.T, F, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [FreeAp]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): FreeAp<F, A> = v as FreeAp<F, A>
    /** Wraps a value into a program with no effects. */
    fun <F, A> pure(v: A): FreeAp<F, A> = Pure(v)
    /** Lifts a single instruction into a [FreeAp] program. */
    fun <F, A> lift(fa: K1<F, A>): FreeAp<F, A> = Ap(fa, Pure { a: A -> a })
    /** Returns the [Applicative] instance for [FreeAp]. */
    fun <F> applicative(): FreeApApplicative<F> = object: FreeApApplicative<F> {}
  }

  /** A program with no effects returning [value]. */
  class Pure<F, A>(val value: A): FreeAp<F, A>() {
    override fun <B> fmap(f: (A) -> B): FreeAp<F, B> = Pure(f(value))
    override fun <B, C> applyTo(v: FreeAp<F, B>, asFunction: (A) -> (B) -> C): FreeAp<F, C> = v.fmap(asFunction(value))
    override fun <G> foldMap(nt: NaturalTransformation<F, G>, applicative: Applicative<G>): K1<G, A> = applicative.pure(value)
    override fun <G> hoist(nt: NaturalTransformation<F, G>): FreeAp<G, A> = Pure(value)
  }

  /** Runs the instruction [fx], then applies the function produced by [next] to its result. */
  class Ap<F, X, A>(val fx: K1<F, X>, val next: FreeAp<F, (X) -> A>): FreeAp<F, A>() {
    override fun <B> fmap(f: (A) -> B): FreeAp<F, B> = Ap(fx, next.fmap { g -> { x: X -> f(g(x)) } })

    override fun <B, C> applyTo(v: FreeAp<F, B>, asFunction: (A) -> (B) -> C): FreeAp<F, C> =
      Ap(fx, next.applyTo(v) { g -> { b: B -> { x: X -> asFunction(g(x))(b) } } })

    override fun <G> foldMap(nt: NaturalTransformation<F, G>, applicative: Applicative<G>): K1<G, A> =
      applicative.ap(applicative.fmap({ x: X -> { g: (X) -> A -> g(x) } }, nt(fx)), next.foldMap(nt, applicative))

    override fun <G> hoist(nt: NaturalTransformation<F, G>): FreeAp<G, A> = Ap(nt(fx), next.hoist(nt))
  }

  /** Maps the result of this program without adding effects. */
  abstract infix fun <B> fmap(f: (A) -> B): FreeAp<F, B>
  /** Interprets every instruction into [applicative] using [nt]. */
  abstract fun <G> foldMap(nt: NaturalTransformation<F, G>, applicative: Applicative<G>): K1<G, A>
  /** Translates the instruction set from `F` to `G` without interpreting. */
  abstract fun <G> hoist(nt: NaturalTransformation<F, G>): FreeAp<G, A>

  /** Applies this program, whose value is turned into a function by [asFunction], to [v] (effects of this come first). */
  internal abstract fun <B, C> applyTo(v: FreeAp<F, B>, asFunction: (A) -> (B) -> C): FreeAp<F, C>

  /** Applies the function produced by [f] to the result of this program; effects of [f] come first. */
  infix fun <B> ap(f: FreeAp<F, (A) -> B>): FreeAp<F, B> = f.applyTo(this) { g -> g }
  /** Interprets the program back into `F` itself when `F` is already an [Applicative]. */
  fun retract(applicative: Applicative<F>): K1<F, A> = foldMap(NaturalTransformation.identity(), applicative)

  /** Statically folds every instruction into the monoid [type] via [nt], without running the program. */
  fun <M: Monoid<*, M>> analyze(type: Class<M>, nt: NaturalTransformation<F, K1<Const.T, M>>): M =
    Const.narrow(foldMap(nt, Const.applicative(type))).value
}
