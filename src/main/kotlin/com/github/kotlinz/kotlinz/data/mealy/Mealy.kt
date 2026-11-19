package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either

/**
 * A Mealy machine: a [step] function that consumes an input and yields an output together with the
 * next machine.
 *
 * Mealy machines are stateful stream transducers that compose like functions.
 * Encoded as K3 with a phantom `Unit` so that it fits `Category<T, F>` / `Arrow<T, F>`.
 *
 * Instances: [MealyFunctor], [MealyCompose], [MealyCategory], [MealyArrow], [MealyArrowChoice],
 * [MealyProfunctor], [MealyStrong].
 *
 * ```kotlin
 * val runningSum = Mealy.unfold(0) { s, i: Int -> Pair(s + i, s + i) }
 * runningSum.run(listOf(1, 2, 3))                                // [1, 3, 6]
 * (runningSum andThen Mealy.arr { it * 10 }).run(listOf(1, 2))  // [10, 30]
 * ```
 *
 * @property step consumes one input, producing an output and the next machine.
 */
class Mealy<I, O>(val step: (I) -> Pair<O, Mealy<I, O>>): K3<Mealy.T, Unit, I, O> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Mealy]. */
    fun <I, O> narrow(v: K1<K2<T, Unit, I>, O>): Mealy<I, O> = v as Mealy<I, O>

    /** Builds a machine from an initial [state] and a transition producing an output and the next state. */
    fun <S, I, O> unfold(state: S, next: (S, I) -> Pair<O, S>): Mealy<I, O> =
      Mealy { i -> next(state, i).let { (o, s) -> Pair(o, unfold(s, next)) } }

    /** The machine that echoes its inputs. */
    fun <A> id(): Mealy<A, A> = arrow.id()
    /** Lifts a stateless function into a machine. */
    fun <A, B> arr(f: (A) -> B): Mealy<A, B> = arrow.arr(f)
    private val arrow = object: MealyArrowChoice {}
  }

  /** Feeds every input to the machine and returns the produced outputs. */
  fun run(inputs: Iterable<I>): List<O> = feedAll(inputs).first
  /** Feeds every input, returning the produced outputs and the resulting machine. */
  fun feedAll(inputs: Iterable<I>): Pair<List<O>, Mealy<I, O>> {
    val outputs = mutableListOf<O>()
    val last = inputs.fold(this) { m, i -> m.step(i).let { (o, next) -> outputs.add(o); next } }
    return Pair(outputs, last)
  }

  // Functor
  /** Maps every output of the machine. */
  infix fun <B> fmap(f: (O) -> B): Mealy<I, B> = object: MealyFunctor<I> {}.fmap(f, this)

  // Arrow
  /** Feeds the outputs of [g] into this machine. */
  infix fun <C> compose(g: Mealy<C, I>): Mealy<C, O> = arrow.compose(this, g)
  /** Feeds the outputs of this machine into [g]. */
  infix fun <C> andThen(g: Mealy<O, C>): Mealy<I, C> = arrow.compose(g, this)
  /** Runs this machine on the first component of each pair. */
  fun <C> first(): Mealy<Pair<I, C>, Pair<O, C>> = arrow.first(this)
  /** Runs this machine on the second component of each pair. */
  fun <C> second(): Mealy<Pair<C, I>, Pair<C, O>> = narrow(arrow.second(this))
  /** Runs this machine and [g] side by side on the components of each pair. */
  infix fun <C, D> split(g: Mealy<C, D>): Mealy<Pair<I, C>, Pair<O, D>> = narrow(arrow.split(this, g))
  /** Feeds every input to both this machine and [g], pairing the outputs. */
  infix fun <C> combine(g: Mealy<I, C>): Mealy<I, Pair<O, C>> = narrow(arrow.combine(this, g))
  /** Runs this machine on `Left` inputs only, passing `Right` inputs through. */
  fun <C> left(): Mealy<Either<I, C>, Either<O, C>> = arrow.left(this)
  /** Runs this machine on `Right` inputs only, passing `Left` inputs through. */
  fun <C> right(): Mealy<Either<C, I>, Either<C, O>> = narrow(arrow.right(this))
  /** Routes `Left` inputs to this machine and `Right` inputs to [g]. */
  infix fun <C, D> splitChoice(g: Mealy<C, D>): Mealy<Either<I, C>, Either<O, D>> = narrow(arrow.splitChoice(this, g))
  /** Routes `Left` inputs to this machine and `Right` inputs to [g], merging the outputs. */
  infix fun <C> fanin(g: Mealy<C, O>): Mealy<Either<I, C>, O> = narrow(arrow.fanin(this, g))
  private val arrow get() = object: MealyArrowChoice {}

  // Profunctor
  /** Pre-composes [f] on the inputs and post-composes [g] on the outputs. */
  fun <J, B> dimap(f: (J) -> I, g: (O) -> B): Mealy<J, B> = profunctor.dimap(f, g, this)
  /** Pre-composes [f] on the inputs. */
  infix fun <J> lmap(f: (J) -> I): Mealy<J, O> = profunctor.lmap(f, this)
  /** Post-composes [g] on the outputs. */
  infix fun <B> rmap(g: (O) -> B): Mealy<I, B> = profunctor.rmap(g, this)
  private val profunctor get() = object: MealyStrong {}
}
