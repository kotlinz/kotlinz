package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Traversable instance for [ListK]. */
interface ListKTraversable: Traversable<ListK.T>, ListKFunctor, ListKFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<ListK.T, A>): K1<G, K1<ListK.T, B>> =
    foldr(applicative.pure(ListK(listOf<B>())), { a: A, acc: K1<G, K1<ListK.T, B>> ->
      applicative.ap(applicative.fmap(prepend(), f(a)), acc)
    }, v)

  private fun <B> prepend(): (B) -> (K1<ListK.T, B>) -> K1<ListK.T, B> =
    { b -> { list -> ListK(listOf(b) + ListK.narrow(list).value) } }
}
