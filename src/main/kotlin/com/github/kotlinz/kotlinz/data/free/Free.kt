package com.github.kotlinz.kotlinz.data.free

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * The free monad over an arbitrary type constructor `F`.
 *
 * Describes a program as a data structure of instructions in `F` that can later be
 * interpreted into any monad with a [NaturalTransformation] via [foldMap]. `F` does not
 * need to be a functor. Use it to separate the description of an effectful DSL from its
 * interpretation (e.g. production vs. test interpreters).
 *
 * Instances: [FreeFunctor], [FreeApplicative], [FreeMonad].
 *
 * ```kotlin
 * fun get(): Free<Counter.T, Int> = Free.liftF(Counter.Get())
 * fun add(n: Int): Free<Counter.T, Unit> = Free.liftF(Counter.Add(n))
 *
 * val program = add(2) bind { get() }
 * val result = State.narrow(program.foldMap(CounterToState, object: StateMonadRec<Int> {})).run(1)
 * ```
 */
sealed class Free<F, A>: K2<Free.T, F, A> {
  class T

  /** A finished program returning [value]. */
  class Pure<F, A>(val value: A): Free<F, A>()
  /** A single suspended instruction [fa]. */
  class Suspend<F, A>(val fa: K1<F, A>): Free<F, A>()
  /** Continues [sub] with the continuation [f]. */
  class FlatMap<F, X, A>(val sub: Free<F, X>, val f: (X) -> Free<F, A>): Free<F, A>()

  companion object {
    /** Downcasts a higher-kinded representation back to [Free]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Free<F, A> = v as Free<F, A>

    /** Lifts a single instruction into a [Free] program. */
    fun <F, A> liftF(fa: K1<F, A>): Free<F, A> = Suspend(fa)

    // Monad
    /** Wraps a value into a program with no instructions. */
    fun <F, A> pure(v: A): Free<F, A> = monad<F>().pure(v)
    /** Flattens a nested program. */
    fun <F, A> join(v: K1<K1<T, F>, K1<K1<T, F>, A>>): Free<F, A> = monad<F>().join(v)
    private fun <F> monad() = object: FreeMonad<F> {}

    /**
     * Builds a program with monad comprehension syntax.
     *
     * Re-runs the block per bound value so that any interpreter, including multi-shot ones, is supported.
     */
    fun <F, A> fx(block: suspend MonadScope<K1<T, F>>.() -> A): Free<F, A> = narrow(monad<F>().fxReplay(block))
  }

  // Monad
  /** Maps the result of this program. */
  infix fun <B> fmap(f: (A) -> B): Free<F, B> = monad.fmap(f, this)
  /** Applies the function produced by [f] to the result of this program. */
  infix fun <B> ap(f: Free<F, (A) -> B>): Free<F, B> = monad.ap(f, this)
  /** Sequences this program with [f]. */
  infix fun <B> bind(f: (A) -> Free<F, B>): Free<F, B> = monad.bind(f, this)
  private val monad get() = object: FreeMonad<F> {}

  /**
   * Interprets this program into [monad] using [nt].
   *
   * Stack-safe when [monad] is a [com.github.kotlinz.kotlinz.type.monad.MonadRec];
   * otherwise stack safety depends on the target's `bind`.
   */
  fun <M> foldMap(nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> = FreeInterpreter.foldMap(this, nt, monad)
}
