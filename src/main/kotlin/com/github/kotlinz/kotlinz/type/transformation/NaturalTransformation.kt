package com.github.kotlinz.kotlinz.type.transformation

import com.github.kotlinz.kotlinz.K1

interface NaturalTransformation<F, G> {
  operator fun <A> invoke(fa: K1<F, A>): K1<G, A>

  infix fun <H> andThen(other: NaturalTransformation<G, H>): NaturalTransformation<F, H> = compose(other, this)

  infix fun <E> compose(other: NaturalTransformation<E, F>): NaturalTransformation<E, G> = compose(this, other)

  companion object {
    fun <F> identity(): NaturalTransformation<F, F> = object: NaturalTransformation<F, F> {
      override fun <A> invoke(fa: K1<F, A>): K1<F, A> = fa
    }

    fun <F, G, H> compose(f: NaturalTransformation<G, H>, g: NaturalTransformation<F, G>): NaturalTransformation<F, H> {
      return object: NaturalTransformation<F, H> {
        override fun <A> invoke(fa: K1<F, A>): K1<H, A> = f(g(fa))
      }
    }
  }
}
