package com.github.kotlinz.kotlinz.data.freer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * The freer (operational) monad over an instruction set `F`, which needs no Functor.
 *
 * A program is either a finished value or an instruction followed by a queue of continuations.
 * Continuations are kept in a type-aligned tree, so `bind` is O(1) and both building and
 * stepping through left-nested programs are stack-safe. Interpret programs with [foldMap] /
 * [interpret], or write a custom interpreter by pattern matching on [view].
 *
 * Instances: [FreerFunctor], [FreerApplicative], [FreerMonad].
 *
 * ```kotlin
 * fun get(): Freer<Counter.T, Int> = Freer.singleton(Counter.Get())
 * val program = Freer.fx<Counter.T, Int> { get().bind() + get().bind() }
 * State.narrow(program.interpret(CounterToState, object : StateMonadRec<Int> {})).run(1)
 * ```
 */
sealed class Freer<F, A>: K2<Freer.T, F, A> {
  class T

  /** A finished program returning [value]. */
  class Pure<F, A>(val value: A): Freer<F, A>()
  internal class Impure<F, A>(val instruction: K1<F, Any?>, val arrows: FreerArrows<F>): Freer<F, A>()

  companion object {
    /** Downcasts a higher-kinded representation back to [Freer]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Freer<F, A> = v as Freer<F, A>

    /** Lifts a single instruction into a program. */
    @Suppress("UNCHECKED_CAST")
    fun <F, A> liftF(fa: K1<F, A>): Freer<F, A> = Impure(fa as K1<F, Any?>, FreerArrows.Leaf { Pure(it) })
    /** Operational-style alias of [liftF]. */
    fun <F, A> singleton(fa: K1<F, A>): Freer<F, A> = liftF(fa)

    // Monad
    /** Wraps a value into a program with no instructions. */
    fun <F, A> pure(v: A): Freer<F, A> = Pure(v)
    /** Flattens a nested program. */
    fun <F, A> join(v: K1<K1<T, F>, K1<K1<T, F>, A>>): Freer<F, A> = monad<F>().join(v)
    /** Builds a program with monad comprehension syntax, replaying the block so multi-shot interpreters work. */
    fun <F, A> fx(block: suspend MonadScope<K1<T, F>>.() -> A): Freer<F, A> = narrow(monad<F>().fxReplay(block))
    private fun <F> monad() = object: FreerMonad<F> {}
  }

  // Monad
  /** Maps the result of this program. */
  infix fun <B> fmap(f: (A) -> B): Freer<F, B> = monad.fmap(f, this)
  /** Applies the function produced by [f] to the result of this program. */
  infix fun <B> ap(f: Freer<F, (A) -> B>): Freer<F, B> = monad.ap(f, this)
  /** Sequences this program with [f]. */
  infix fun <B> bind(f: (A) -> Freer<F, B>): Freer<F, B> = monad.bind(f, this)
  private val monad get() = object: FreerMonad<F> {}

  /** Exposes the first step of this program for writing custom interpreters. */
  @Suppress("UNCHECKED_CAST")
  fun view(): FreerView<F, A> = when (this) {
    is Pure -> FreerView.Return(value)
    is Impure -> FreerView.Bind(instruction) { x -> arrows.applyTo(x) as Freer<F, A> }
  }

  /**
   * Interprets this program into [monad] using [nt].
   *
   * Stack-safe when [monad] is a [com.github.kotlinz.kotlinz.type.monad.MonadRec].
   */
  fun <M> foldMap(nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> = FreerInterpreter.foldMap(this, nt, monad)
  /** Operational-style alias of [foldMap]. */
  fun <M> interpret(nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> = foldMap(nt, monad)
}
