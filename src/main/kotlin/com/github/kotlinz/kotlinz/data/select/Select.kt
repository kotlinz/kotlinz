package com.github.kotlinz.kotlinz.data.select

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.selectt.SelectT
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay

/**
 * The selection monad: `((A) -> R) -> A`, a choice of an `A` given a way to rank each candidate by an outcome `R`.
 *
 * Equivalent to [SelectT] over [Identity] (see [toSelectT]). Sequencing selections makes each choice knowing how
 * the rest of the computation scores it, which solves greedy optimisation and game-playing problems.
 * Instances: [SelectFunctor], [SelectApplicative], [SelectMonad].
 *
 * ```kotlin
 * fun argmax(xs: List<Int>) = Select<Int, Int> { k -> xs.maxBy(k) }
 * val pair = argmax(listOf(1, 2, 3)) bind { x -> argmax(listOf(1, 2, 3)) fmap { y -> Pair(x, y) } }
 * pair.run { (x, y) -> x - y } // Pair(3, 1)
 * ```
 *
 * @property run the selection function.
 */
class Select<R, A>(val run: ((A) -> R) -> A): K2<Select.T, R, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded value to its concrete type. */
    fun <R, A> narrow(v: K1<K1<T, R>, A>): Select<R, A> = v as Select<R, A>

    // Monad
    /** Selects `v` regardless of its outcome. */
    fun <R, A> pure(v: A): Select<R, A> = monad<R>().pure(v)
    /** Flattens a nested Select. */
    fun <R, A> join(v: K1<K1<T, R>, K1<K1<T, R>, A>>): Select<R, A> = monad<R>().join(v)
    /** Builds a selection with monad comprehension syntax; the block is replayed per bound value. */
    fun <R, A> fx(block: suspend MonadScope<K1<T, R>>.() -> A): Select<R, A> = narrow(monad<R>().fxReplay(block))
    private fun <R> monad() = object: SelectMonad<R> {}
  }

  // Monad
  /** Maps the selected value; the ranking sees the mapped value. */
  infix fun <B> fmap(f: (A) -> B): Select<R, B> = monad.fmap(f, this)
  /** Selects a function, then a value for it. */
  infix fun <B> ap(f: Select<R, (A) -> B>): Select<R, B> = monad.ap(f, this)
  /** Sequences this selection with `f`, choosing knowing how `f` continues. */
  infix fun <B> bind(f: (A) -> Select<R, B>): Select<R, B> = monad.bind(f, this)
  private val monad get() = object: SelectMonad<R> {}

  /** Converts to [SelectT] over [Identity]. */
  fun toSelectT(): SelectT<R, Identity.T, A> =
    SelectT(object: IdentityMonad {}) { k -> Identity(run { a -> Identity.narrow(k(a)).value }) }
}
