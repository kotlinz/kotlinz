package com.github.kotlinz.kotlinz.data.moore

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * A Moore machine: a current [output] together with a [step] function that consumes an input and
 * yields the next machine.
 *
 * Useful as a resumable left fold whose intermediate result is always observable.
 * Encoded as K3 with a phantom `Unit` so that it fits `Profunctor<T, F>`.
 *
 * Instances: [MooreFunctor], [MooreApplicative], [MooreCopointed], [MooreComonad], [MooreProfunctor].
 *
 * ```kotlin
 * val sum = Moore.unfold(0, { s -> s }) { s, i: Int -> s + i }
 * sum.run(listOf(1, 2, 3))                          // 6
 * sum.scan(listOf(1, 2, 3))                         // [0, 1, 3, 6]
 * (sum lmap String::length).run(listOf("ab", "c"))  // 3
 * ```
 *
 * @property output the output of the current state.
 * @property step the transition to the next machine.
 */
class Moore<I, O>(val output: O, val step: (I) -> Moore<I, O>): K3<Moore.T, Unit, I, O> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Moore]. */
    fun <I, O> narrow(v: K1<K2<T, Unit, I>, O>): Moore<I, O> = v as Moore<I, O>

    /** Builds a machine from an initial [state], an [output] function and a transition function [next]. */
    fun <S, I, O> unfold(state: S, output: (S) -> O, next: (S, I) -> S): Moore<I, O> =
      Moore(output(state)) { i -> unfold(next(state, i), output, next) }

    /** A machine that ignores its inputs and always outputs [o]. */
    fun <I, O> pure(o: O): Moore<I, O> = object: MooreApplicative<I> {}.pure(o)
  }

  /** Feeds every input to the machine and returns the final output. */
  fun run(inputs: Iterable<I>): O = feedAll(inputs).output
  /** Feeds every input to the machine and returns the resulting machine. */
  fun feedAll(inputs: Iterable<I>): Moore<I, O> = inputs.fold(this) { m, i -> m.step(i) }
  /** Returns every output observed while feeding the inputs, starting with the current one. */
  fun scan(inputs: Iterable<I>): List<O> = inputs.runningFold(this) { m, i -> m.step(i) }.map { it.output }

  // Applicative
  /** Maps every output of the machine. */
  infix fun <B> fmap(f: (O) -> B): Moore<I, B> = applicative.fmap(f, this)
  /** Runs [f] and this machine in lockstep, applying the functions to the outputs. */
  infix fun <B> ap(f: Moore<I, (O) -> B>): Moore<I, B> = applicative.ap(f, this)
  private val applicative get() = object: MooreApplicative<I> {}

  // Comonad
  /** Returns the current output. */
  fun extract(): O = comonad.extract(this)
  /** Returns a machine whose outputs are the machines reachable from this one. */
  fun duplicate(): Moore<I, Moore<I, O>> = comonad.duplicate(this) fmap { narrow(it) }
  /** Applies [f] to every machine reachable from this one. */
  infix fun <B> extend(f: (Moore<I, O>) -> B): Moore<I, B> = comonad.extend({ w -> f(narrow(w)) }, this)
  private val comonad get() = object: MooreComonad<I> {}

  // Profunctor
  /** Pre-composes [f] on the inputs and post-composes [g] on the outputs. */
  fun <J, B> dimap(f: (J) -> I, g: (O) -> B): Moore<J, B> = profunctor.dimap(f, g, this)
  /** Pre-composes [f] on the inputs. */
  infix fun <J> lmap(f: (J) -> I): Moore<J, O> = profunctor.lmap(f, this)
  /** Post-composes [g] on the outputs. */
  infix fun <B> rmap(g: (O) -> B): Moore<I, B> = profunctor.rmap(g, this)
  private val profunctor get() = object: MooreProfunctor {}
}
