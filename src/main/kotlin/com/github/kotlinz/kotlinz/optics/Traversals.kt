package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKTraversable
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeTraversable
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListTraversable
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.profunctor.Traversing
import com.github.kotlinz.kotlinz.type.profunctor.Wander

/** The traversal over every element of a [traversable] container. */
fun <F, A, B> Traversal.Companion.fromTraversable(traversable: Traversable<F>): Traversal<K1<F, A>, K1<F, B>, A, B> =
  object : Traversal<K1<F, A>, K1<F, B>, A, B> {
    override fun <P, G> runTraversing(traversing: Traversing<P, G>, pab: K3<P, G, A, B>) = traversing.traverse(traversable, pab)
  }

/** The traversal over every element of a [List]. */
fun <A, B> Traversal.Companion.list(): Traversal<List<A>, List<B>, A, B> =
  Iso({ l: List<A> -> ListK(l) }, { l: ListK<B> -> l.value }) andThen Traversal.listK()

/** The traversal over every element of a [ListK]. */
fun <A, B> Traversal.Companion.listK(): Traversal<ListK<A>, ListK<B>, A, B> = narrowed(object : ListKTraversable {}) { v: K1<ListK.T, B> -> ListK.narrow(v) }

/** The traversal over every element of a [NonEmptyList]. */
fun <A, B> Traversal.Companion.nonEmptyList(): Traversal<NonEmptyList<A>, NonEmptyList<B>, A, B> =
  narrowed(object : NonEmptyListTraversable {}) { v: K1<NonEmptyList.T, B> -> NonEmptyList.narrow(v) }

/** The traversal over the value of a [Maybe], if any. */
fun <A, B> Traversal.Companion.maybe(): Traversal<Maybe<A>, Maybe<B>, A, B> = narrowed(object : MaybeTraversable {}) { v: K1<Maybe.T, B> -> Maybe.narrow(v) }

private fun <F, A, B, FA : K1<F, A>, FB> narrowed(traversable: Traversable<F>, narrow: (K1<F, B>) -> FB): Traversal<FA, FB, A, B> =
  Traversal(object : Wander<FA, FB, A, B> {
    override fun <G> run(applicative: Applicative<G>, f: (A) -> K1<G, B>, s: FA): K1<G, FB> =
      applicative.fmap(narrow, traversable.traverse(applicative, f, s))
  })
