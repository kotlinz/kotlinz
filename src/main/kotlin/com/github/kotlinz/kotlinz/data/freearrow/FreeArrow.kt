package com.github.kotlinz.kotlinz.data.freearrow

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.freecategory.QuiverTransformation
import com.github.kotlinz.kotlinz.type.arrow.Arrow

/**
 * The free arrow over a quiver `P`: plain functions ([arr]), quiver arrows ([lift]), composition and
 * [first], kept as data until interpreted with [foldMap] into any [Arrow].
 *
 * Instances: [FreeArrowCompose], [FreeArrowCategory], [FreeArrowArrow].
 *
 * ```kotlin
 * val both = FreeArrow.lift(Step.Double) split FreeArrow.arr { s: String -> s.length }
 * Function1.narrow(both.foldMap(object : Function1Arrow {}, toFunction1))(Pair(2, "ab")) // (4, 2)
 * ```
 */
sealed class FreeArrow<P, A, B>: K3<FreeArrow.T, P, A, B> {
  class T

  /** A plain function. */
  class Arr<P, A, B>(val f: (A) -> B): FreeArrow<P, A, B>()
  /** A single quiver arrow. */
  class Lift<P, A, B>(val arrow: K2<P, A, B>): FreeArrow<P, A, B>()
  /** Runs [g] and then [f]. */
  class Compose<P, A, B, C>(val f: FreeArrow<P, B, C>, val g: FreeArrow<P, A, B>): FreeArrow<P, A, C>()
  /** Runs [f] on the first component of a pair. */
  class First<P, A, B, C>(val f: FreeArrow<P, A, B>): FreeArrow<P, Pair<A, C>, Pair<B, C>>()

  companion object {
    /** Downcasts a higher-kinded value back to [FreeArrow]. */
    fun <P, A, B> narrow(v: K1<K2<T, P, A>, B>): FreeArrow<P, A, B> = v as FreeArrow<P, A, B>
    /** The identity arrow. */
    fun <P, A> id(): FreeArrow<P, A, A> = arrow<P>().id()
    /** Lifts a plain function. */
    fun <P, A, B> arr(f: (A) -> B): FreeArrow<P, A, B> = arrow<P>().arr(f)
    /** Lifts a single quiver arrow. */
    fun <P, A, B> lift(arrow: K2<P, A, B>): FreeArrow<P, A, B> = Lift(arrow)
    private fun <P> arrow() = object: FreeArrowArrow<P> {}
  }

  /** Interprets this arrow in [arrow], mapping every quiver arrow with [nt]. */
  fun <T, F> foldMap(arrow: Arrow<T, F>, nt: QuiverTransformation<P, T, F>): K3<T, F, A, B> = interpret(this, arrow, nt)

  /** Runs [g] first, then this arrow. */
  infix fun <C> compose(g: FreeArrow<P, C, A>): FreeArrow<P, C, B> = arrow.compose(this, g)
  /** Runs this arrow first, then [g]. */
  infix fun <C> andThen(g: FreeArrow<P, B, C>): FreeArrow<P, A, C> = arrow.compose(g, this)
  /** Runs this arrow on the first component of a pair. */
  fun <C> first(): FreeArrow<P, Pair<A, C>, Pair<B, C>> = arrow.first(this)
  /** Runs this arrow on the second component of a pair. */
  fun <C> second(): FreeArrow<P, Pair<C, A>, Pair<C, B>> = narrow(arrow.second(this))
  /** Runs this arrow and [g] on the respective components of a pair. */
  infix fun <C, D> split(g: FreeArrow<P, C, D>): FreeArrow<P, Pair<A, C>, Pair<B, D>> = narrow(arrow.split(this, g))
  /** Feeds the same input to this arrow and [g], pairing the results. */
  infix fun <C> combine(g: FreeArrow<P, A, C>): FreeArrow<P, A, Pair<B, C>> = narrow(arrow.combine(this, g))
  private val arrow get() = object: FreeArrowArrow<P> {}
}

@Suppress("UNCHECKED_CAST")
private fun <P, A, B, T, F> interpret(free: FreeArrow<P, A, B>, arrow: Arrow<T, F>, nt: QuiverTransformation<P, T, F>): K3<T, F, A, B> =
  when (free) {
    is FreeArrow.Arr -> arrow.arr(free.f)
    is FreeArrow.Lift -> nt(free.arrow)
    is FreeArrow.Compose<P, A, *, B> -> (free as FreeArrow.Compose<P, A, Any?, B>).let { arrow.compose(interpret(it.f, arrow, nt), interpret(it.g, arrow, nt)) }
    is FreeArrow.First<P, *, *, *> -> (free as FreeArrow.First<P, Any?, Any?, Any?>).let { arrow.first<Any?, Any?, Any?>(interpret(it.f, arrow, nt)) } as K3<T, F, A, B>
  }
