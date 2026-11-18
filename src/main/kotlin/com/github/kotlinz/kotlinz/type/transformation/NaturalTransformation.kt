package com.github.kotlinz.kotlinz.type.transformation

import com.github.kotlinz.kotlinz.K1

/**
 * A natural transformation `F ~> G`: a mapping from `F<A>` to `G<A>` that works uniformly for every `A`.
 *
 * Law:
 * - Naturality: `invoke(F.fmap(f, fa)) == G.fmap(f, invoke(fa))`
 *
 * @see com.github.kotlinz.kotlinz.type.Functor
 */
interface NaturalTransformation<F, G> {
  /** Transforms `fa` into the target functor. */
  operator fun <A> invoke(fa: K1<F, A>): K1<G, A>

  /** Runs this transformation and then `other`. */
  infix fun <H> andThen(other: NaturalTransformation<G, H>): NaturalTransformation<F, H> = compose(other, this)

  /** Runs `other` and then this transformation. */
  infix fun <E> compose(other: NaturalTransformation<E, F>): NaturalTransformation<E, G> = compose(this, other)

  companion object {
    /** The identity transformation. */
    fun <F> identity(): NaturalTransformation<F, F> = object: NaturalTransformation<F, F> {
      override fun <A> invoke(fa: K1<F, A>): K1<F, A> = fa
    }

    /** Composes right to left: the result runs `g` and then `f`. */
    fun <F, G, H> compose(f: NaturalTransformation<G, H>, g: NaturalTransformation<F, G>): NaturalTransformation<F, H> {
      return object: NaturalTransformation<F, H> {
        override fun <A> invoke(fa: K1<F, A>): K1<H, A> = f(g(fa))
      }
    }
  }
}
