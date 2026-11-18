package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Traversable instance for [NonEmptyList]. */
interface NonEmptyListTraversable: Traversable<NonEmptyList.T>, NonEmptyListFunctor, NonEmptyListFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<NonEmptyList.T, A>): K1<G, K1<NonEmptyList.T, B>> {
    val list = NonEmptyList.narrow(v)
    val tail = list.tail.foldRight(applicative.pure(listOf<B>())) { a, acc ->
      applicative.ap(applicative.fmap({ b: B -> { bs: List<B> -> listOf(b) + bs } }, f(a)), acc)
    }
    return applicative.ap(applicative.fmap(cons(), f(list.head)), tail)
  }

  private fun <B> cons(): (B) -> (List<B>) -> K1<NonEmptyList.T, B> = { head -> { tail -> NonEmptyList(head, tail) } }
}
