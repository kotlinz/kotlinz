package com.github.kotlinz.kotlinz.data.fold

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * A composable left fold over inputs [A] producing a result [B]: an initial state, a step function
 * and a final extraction, with the state type hidden.
 *
 * Folds combine with [ap] / [zip] so that several summaries are computed in a single pass.
 * Encoded as K3 with a phantom `Unit` so that it fits `Profunctor<T, F>`.
 *
 * Instances: [FoldFunctor], [FoldApplicative], [FoldProfunctor]. Common folds such as
 * [sum], [length], [maximum] and [list] are companion extensions.
 *
 * ```kotlin
 * val mean = Fold.sum().zip(Fold.length<Int>()) fmap { (s, n) -> s / n }
 * mean.run(listOf(2, 4, 6))   // 4
 * Fold.list<Int>().scan(listOf(1, 2)) // [[], [1], [1, 2]]
 * ```
 */
class Fold<A, B> private constructor(
  internal val initial: Any?,
  internal val step: (Any?, A) -> Any?,
  internal val done: (Any?) -> B
): K3<Fold.T, Unit, A, B> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Fold]. */
    fun <A, B> narrow(v: K1<K2<T, Unit, A>, B>): Fold<A, B> = v as Fold<A, B>

    /** Builds a fold from an [initial] state, a [step] function and an [extract] function. */
    @Suppress("UNCHECKED_CAST")
    fun <S, A, B> of(initial: S, step: (S, A) -> S, extract: (S) -> B): Fold<A, B> =
      Fold(initial, { s, a -> step(s as S, a) }, { s -> extract(s as S) })

    /** A fold that ignores its inputs and returns [b]. */
    fun <A, B> pure(b: B): Fold<A, B> = object: FoldApplicative<A> {}.pure(b)
  }

  /** Folds every input and returns the result. */
  fun run(inputs: Iterable<A>): B = done(inputs.fold(initial, step))
  /** Returns the result after each prefix of the inputs, starting with the empty prefix. */
  fun scan(inputs: Iterable<A>): List<B> = inputs.runningFold(initial, step).map(done)

  // Applicative
  /** Maps the result of the fold. */
  infix fun <C> fmap(f: (B) -> C): Fold<A, C> = applicative.fmap(f, this)
  /** Runs [f] and this fold in a single pass, applying the resulting function to this result. */
  infix fun <C> ap(f: Fold<A, (B) -> C>): Fold<A, C> = applicative.ap(f, this)
  /** Runs this fold and [other] in a single pass, pairing the results. */
  infix fun <C> zip(other: Fold<A, C>): Fold<A, Pair<B, C>> = other ap (this fmap { b -> { c: C -> Pair(b, c) } })
  private val applicative get() = object: FoldApplicative<A> {}

  // Profunctor
  /** Pre-composes [f] on the inputs and post-composes [g] on the result. */
  fun <Z, C> dimap(f: (Z) -> A, g: (B) -> C): Fold<Z, C> = profunctor.dimap(f, g, this)
  /** Pre-composes [f] on the inputs. */
  infix fun <Z> lmap(f: (Z) -> A): Fold<Z, B> = profunctor.lmap(f, this)
  /** Post-composes [g] on the result. */
  infix fun <C> rmap(g: (B) -> C): Fold<A, C> = profunctor.rmap(g, this)
  private val profunctor get() = object: FoldProfunctor {}
}
