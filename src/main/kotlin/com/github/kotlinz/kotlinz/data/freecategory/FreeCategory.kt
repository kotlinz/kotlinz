package com.github.kotlinz.kotlinz.data.freecategory

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Category

/**
 * The free category over a quiver `P`: paths of `P` arrows (`K2<P, A, B>`) built with [id],
 * [lift] and composition, kept as data until interpreted with [foldMap].
 *
 * Use it to describe a pipeline of abstract steps once and run it in any [Category].
 *
 * Instances: [FreeCategoryCompose], [FreeCategoryCategory].
 *
 * ```kotlin
 * val pipeline = FreeCategory.lift(Step.Parse) andThen FreeCategory.lift(Step.Double)
 * Function1.narrow(pipeline.foldMap(object : Function1Category {}, toFunction1))("21") // 42
 * ```
 */
sealed class FreeCategory<P, A, B>: K3<FreeCategory.T, P, A, B> {
  class T

  /** The empty path. */
  class Id<P, A>: FreeCategory<P, A, A>()
  /** A path made of the single arrow [arrow]. */
  class Lift<P, A, B>(val arrow: K2<P, A, B>): FreeCategory<P, A, B>()
  /** The path [g] followed by the path [f]. */
  class Compose<P, A, B, C>(val f: FreeCategory<P, B, C>, val g: FreeCategory<P, A, B>): FreeCategory<P, A, C>()

  companion object {
    /** Downcasts a higher-kinded value back to [FreeCategory]. */
    fun <P, A, B> narrow(v: K1<K2<T, P, A>, B>): FreeCategory<P, A, B> = v as FreeCategory<P, A, B>
    /** The empty path. */
    fun <P, A> id(): FreeCategory<P, A, A> = Id()
    /** Lifts a single quiver arrow into a path. */
    fun <P, A, B> lift(arrow: K2<P, A, B>): FreeCategory<P, A, B> = Lift(arrow)
  }

  /** Interprets the path in [category], mapping every arrow with [nt]. */
  @Suppress("UNCHECKED_CAST")
  fun <T, F> foldMap(category: Category<T, F>, nt: QuiverTransformation<P, T, F>): K3<T, F, A, B> = when (this) {
    is Id<*, *> -> category.id<A>() as K3<T, F, A, B>
    is Lift -> nt(arrow)
    is Compose<P, A, *, B> -> (this as Compose<P, A, Any?, B>).let { category.compose(it.f.foldMap(category, nt), it.g.foldMap(category, nt)) }
  }

  /** Runs [g] first, then this path. */
  infix fun <C> compose(g: FreeCategory<P, C, A>): FreeCategory<P, C, B> = Compose(this, g)
  /** Runs this path first, then [g]. */
  infix fun <C> andThen(g: FreeCategory<P, B, C>): FreeCategory<P, A, C> = Compose(g, this)
}
