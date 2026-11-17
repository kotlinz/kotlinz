package com.github.kotlinz.kotlinz.data.day

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.monad.Comonad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

// Day convolution: exists B C. (F<B>, G<C>, (B, C) -> A). The existential types are erased internally.
@Suppress("UNCHECKED_CAST")
class Day<F, G, A> private constructor(
  internal val left: K1<F, Any?>,
  internal val right: K1<G, Any?>,
  internal val combine: (Any?, Any?) -> A
): K3<Day.T, F, G, A> {
  class T

  interface Fold<F, G, A, R> {
    operator fun <B, C> invoke(left: K1<F, B>, right: K1<G, C>, combine: (B, C) -> A): R
  }

  companion object {
    fun <F, G, A> narrow(v: K1<K2<T, F, G>, A>): Day<F, G, A> = v as Day<F, G, A>

    operator fun <F, G, B, C, A> invoke(left: K1<F, B>, right: K1<G, C>, combine: (B, C) -> A): Day<F, G, A> =
      Day(left = left as K1<F, Any?>, right = right as K1<G, Any?>, combine = { b, c -> combine(b as B, c as C) })

    fun <F, G, A, B> day(fa: K1<F, A>, gb: K1<G, B>): Day<F, G, Pair<A, B>> = Day(fa, gb, ::Pair)

    fun <F, G> functor(): DayFunctor<F, G> = object: DayFunctor<F, G> {}
    fun <F, G> applicative(first: Applicative<F>, second: Applicative<G>): DayApplicative<F, G> =
      DayApplicative.of(first, second)
    fun <F, G> comonad(first: Comonad<F>, second: Comonad<G>): DayComonad<F, G> = DayComonad.of(first, second)
  }

  fun <R> fold(f: Fold<F, G, A, R>): R = f(left, right, combine)

  infix fun <B> fmap(f: (A) -> B): Day<F, G, B> = functor<F, G>().fmap(f, this)
  fun swapped(): Day<G, F, A> = Day(right, left) { c: Any?, b: Any? -> combine(b, c) }
  fun <H> trans1(nt: NaturalTransformation<F, H>): Day<H, G, A> = Day(nt(left), right, combine)
  fun <H> trans2(nt: NaturalTransformation<G, H>): Day<F, H, A> = Day(left, nt(right), combine)
}

fun <F, A> Day<F, F, A>.dap(applicative: Applicative<F>): K1<F, A> =
  applicative.ap(applicative.fmap({ b: Any? -> { c: Any? -> combine(b, c) } }, left), right)
