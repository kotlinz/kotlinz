package com.github.kotlinz.kotlinz.data.codensity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.pointed.Pointed

/**
 * The codensity monad: a continuation-passing wrapper `forall R. (A -> F<R>) -> F<R>` around a monad F.
 *
 * Use it to improve the performance of left-nested binds (for example in free monads or list-like
 * monads): [lift] into Codensity, build the computation, then [lower] back. Binds are right-associated
 * automatically. Has instances for Functor, Applicative and Monad (see [CodensityMonad]); these need no
 * instance of F, only [lift] and [lower] do.
 *
 * ```kotlin
 * val listMonad = object : ListKMonad {}
 * val m = Codensity.lift(listMonad, listKOf(1, 2)) bind { x -> Codensity.pure<ListK.T, Int>(x * 10) }
 * m.lower(listMonad) // ListK [10, 20]
 * ```
 */
abstract class Codensity<F, A>: K2<Codensity.T, F, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  /** Runs the computation with the continuation [k]. */
  abstract fun <R> run(k: (A) -> K1<F, R>): K1<F, R>

  companion object {
    /** Downcasts the higher-kinded representation back to [Codensity]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Codensity<F, A> = v as Codensity<F, A>

    /** Lifts a value of the underlying [monad] into Codensity. */
    fun <F, A> lift(monad: Monad<F>, fa: K1<F, A>): Codensity<F, A> = object: Codensity<F, A>() {
      override fun <R> run(k: (A) -> K1<F, R>): K1<F, R> = monad.bind(k, fa)
    }

    // Monad
    /** Wraps a pure value. */
    fun <F, A> pure(v: A): Codensity<F, A> = monad<F>().pure(v)
    /** Flattens a nested Codensity. */
    fun <F, A> join(v: K1<K1<T, F>, K1<K1<T, F>, A>>): Codensity<F, A> = monad<F>().join(v)
    private fun <F> monad() = object: CodensityMonad<F> {}
  }

  /** Runs the computation back into F using [pointed] as the final continuation. */
  fun lower(pointed: Pointed<F>): K1<F, A> = run { a -> pointed.pure(a) }

  // Monad
  /** Maps over the result. */
  infix fun <B> fmap(f: (A) -> B): Codensity<F, B> = monad.fmap(f, this)
  /** Applies a wrapped function to the result. */
  infix fun <B> ap(f: Codensity<F, (A) -> B>): Codensity<F, B> = monad.ap(f, this)
  /** Sequences a dependent computation. */
  infix fun <B> bind(f: (A) -> Codensity<F, B>): Codensity<F, B> = monad.bind(f, this)
  private val monad get() = object: CodensityMonad<F> {}
}
