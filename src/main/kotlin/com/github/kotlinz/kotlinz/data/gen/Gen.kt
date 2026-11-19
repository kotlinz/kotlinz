package com.github.kotlinz.kotlinz.data.gen

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A pure random generator: a [State] computation over a SplitMix64 [Seed].
 *
 * Running a generator with the same seed always produces the same value and next seed, so random
 * programs are reproducible and testable. Building on [State] reuses its threading of the seed,
 * while the dedicated [Seed] provides good statistical quality without any mutable `Random`.
 *
 * Instances: [GenFunctor], [GenApplicative], [GenMonad].
 *
 * ```kotlin
 * val point = Gen.fx { Pair(Gen.choose(0, 9).bind(), Gen.boolean().bind()) }
 * point.sample(42L) == point.sample(42L) // true
 * Gen.listOf(3, Gen.oneOf(Gen.pure("a"), Gen.pure("b"))).sample(1L) // e.g. [b, a, a]
 * ```
 *
 * @property state the underlying seed-threading computation.
 */
class Gen<A>(val state: State<Seed, A>): K1<Gen.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Gen]. */
    fun <A> narrow(v: K1<T, A>): Gen<A> = v as Gen<A>

    // Monad
    /** A generator that always yields [v] and leaves the seed untouched. */
    fun <A> pure(v: A): Gen<A> = monad.pure(v)
    /** Flattens a generator of generators. */
    fun <A> join(v: K1<T, K1<T, A>>): Gen<A> = monad.join(v)
    private val monad = object: GenMonad {}
    /** Builds a [Gen] with monad comprehension syntax. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Gen<A> = narrow(monad.fx(block))

    // Primitives
    /** Uniformly distributed 64-bit values. */
    fun long(): Gen<Long> = Gen(State { seed -> seed.next() })
    /** Uniformly distributed 32-bit values. */
    fun int(): Gen<Int> = long() fmap { (it ushr 32).toInt() }
    /** `true` or `false` with equal probability. */
    fun boolean(): Gen<Boolean> = long() fmap { it < 0 }
    /** Uniformly distributed doubles in `[0, 1)`. */
    fun double(): Gen<Double> = long() fmap { (it ushr 11) * DOUBLE_UNIT }
    /** Integers in the inclusive range `[min, max]` (modulo bias is below 2^-32). */
    fun choose(min: Int, max: Int): Gen<Int> = long() fmap { min + Math.floorMod(it, max.toLong() - min + 1).toInt() }
    /** Integers in [range]. */
    fun int(range: IntRange): Gen<Int> = choose(range.first, range.last)

    // Combinators
    /** Picks one of [values] uniformly. */
    fun <A> elements(vararg values: A): Gen<A> = choose(0, values.size - 1) fmap { values[it] }
    /** Picks one of [gens] uniformly and runs it. */
    fun <A> oneOf(vararg gens: Gen<A>): Gen<A> = elements(*gens) bind { it }
    /** Runs [gen] [size] times, threading the seed, and collects the results. */
    fun <A> listOf(size: Int, gen: Gen<A>): Gen<List<A>> = Gen(State { seed -> collect(size, gen, seed) })
    /** Generates a list whose length is drawn from [size]. */
    fun <A> listOf(size: Gen<Int>, gen: Gen<A>): Gen<List<A>> = size bind { listOf(it, gen) }

    private const val DOUBLE_UNIT = 1.0 / (1L shl 53)

    private fun <A> collect(size: Int, gen: Gen<A>, seed: Seed): Pair<List<A>, Seed> =
      (1..size).fold(Pair(emptyList(), seed)) { (acc, s), _ -> gen.run(s).let { (a, next) -> Pair(acc + a, next) } }
  }

  // Monad
  /** Maps the generated value. */
  infix fun <B> fmap(f: (A) -> B): Gen<B> = monad.fmap(f, this)
  /** Runs [f], then this generator, and applies the function to the value. */
  infix fun <B> ap(f: Gen<(A) -> B>): Gen<B> = monad.ap(f, this)
  /** Runs this generator, then the generator chosen by [f] with the advanced seed. */
  infix fun <B> bind(f: (A) -> Gen<B>): Gen<B> = monad.bind(f, this)
  private val monad = object: GenMonad {}

  /** Generates a value from [seed], returning it with the advanced seed. */
  fun run(seed: Seed): Pair<A, Seed> = state.run(seed)
  /** Generates a value from the raw seed [seed]. */
  fun sample(seed: Long): A = run(Seed(seed)).first
}
