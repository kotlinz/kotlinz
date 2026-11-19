package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Traversable instance for [LazyList]; it forces the whole list, so it only terminates for finite lists. */
interface LazyListTraversable: Traversable<LazyList.T>, LazyListFunctor, LazyListFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<LazyList.T, A>): K1<G, K1<LazyList.T, B>> =
    foldr(applicative.pure(LazyList.empty<B>()), { a: A, acc: K1<G, K1<LazyList.T, B>> ->
      applicative.ap(applicative.fmap(prepend(), f(a)), acc)
    }, v)

  private fun <B> prepend(): (B) -> (K1<LazyList.T, B>) -> K1<LazyList.T, B> =
    { b -> { list -> LazyList.cons(b) { LazyList.narrow(list) } } }
}
