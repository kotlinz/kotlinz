package com.github.kotlinz.kotlinz.data.yoneda

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/**
 * The Yoneda embedding of a functor `F`: `forall B. ((A) -> B) -> F<B>`.
 *
 * `Yoneda<F, A>` is isomorphic to `F<A>` but is a [Functor] for any `F` and fuses consecutive
 * `fmap` calls into a single `fmap` on the underlying functor when [lower]ed. Use it to optimise
 * chains of maps over an expensive functor.
 *
 * Instances: [YonedaFunctor].
 *
 * ```kotlin
 * val y = Yoneda.lift(object : ListKFunctor {}, listKOf(1, 2, 3))
 * y.fmap { it + 1 }.fmap { it.toString() }.lower()   // listKOf("2", "3", "4"), with a single underlying fmap
 * ```
 */
abstract class Yoneda<F, A>: K2<Yoneda.T, F, A> {
  /** Higher-kinded witness for [Yoneda]. */
  class T

  /** Maps the embedded value with [f] in the underlying functor. */
  abstract fun <B> run(f: (A) -> B): K1<F, B>

  companion object {
    /** Downcasts a higher-kinded value to [Yoneda]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Yoneda<F, A> = v as Yoneda<F, A>

    /** Embeds `fa` into [Yoneda] using [functor]. */
    fun <F, A> lift(functor: Functor<F>, fa: K1<F, A>): Yoneda<F, A> = object: Yoneda<F, A>() {
      override fun <B> run(f: (A) -> B): K1<F, B> = functor.fmap(f, fa)
    }
  }

  /** Converts back to the underlying `F<A>`. */
  fun lower(): K1<F, A> = run { it }

  // Functor
  /** Composes [f] without touching the underlying functor. */
  infix fun <B> fmap(f: (A) -> B): Yoneda<F, B> = functor.fmap(f, this)
  private val functor get() = object: YonedaFunctor<F> {}
}
