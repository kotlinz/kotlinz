package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * A strong, choice profunctor that can be lifted through any traversal (the basis of traversals).
 *
 * Laws:
 * - Identity: `wander(identity, v) == v` where `identity` applies `f` to `s` directly
 * - Composition: `wander(outer, wander(inner, v)) == wander(outer andThen inner, v)`
 *
 * ```kotlin
 * val traversing = object : Function1Traversing {}
 * val listK = object : ListKTraversable {}
 * Function1.narrow(traversing.traverse(listK, Function1 { n: Int -> n + 1 }))(listKOf(1, 2)) // ListK([2, 3])
 * ```
 *
 * @see Wander
 * @see Strong
 * @see Choice
 */
interface Traversing<T, F> : Strong<T, F>, Choice<T, F> {
  /** Lifts `v` to act on every focus visited by [traversal]. */
  fun <S, R, A, B> wander(traversal: Wander<S, R, A, B>, v: K3<T, F, A, B>): K3<T, F, S, R>

  /** Lifts `v` to act on every element of a [traversable] container (`traverse'`). */
  fun <G, A, B> traverse(traversable: Traversable<G>, v: K3<T, F, A, B>): K3<T, F, K1<G, A>, K1<G, B>> =
    wander(object : Wander<K1<G, A>, K1<G, B>, A, B> {
      override fun <H> run(applicative: Applicative<H>, f: (A) -> K1<H, B>, s: K1<G, A>): K1<H, K1<G, B>> =
        traversable.traverse(applicative, f, s)
    }, v)
}
