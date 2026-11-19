package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * A van Laarhoven traversal `forall F. Applicative<F> -> ((A) -> F<B>) -> (S) -> F<T>`.
 *
 * Kotlin has no rank-2 types, so the quantification over `F` is expressed as a generic method.
 * It is the argument of [Traversing.wander].
 *
 * Laws:
 * - Identity: `run(Identity, ::Identity, s) == Identity(s)`
 * - Composition: effects of `run(Compose(F, G), ...)` match running with `F` then `G`.
 *
 * ```kotlin
 * val both = object : Wander<Pair<Int, Int>, Pair<String, String>, Int, String> {
 *   override fun <F> run(applicative: Applicative<F>, f: (Int) -> K1<F, String>, s: Pair<Int, Int>) =
 *     applicative.ap(applicative.fmap({ a: String -> { b: String -> Pair(a, b) } }, f(s.first)), f(s.second))
 * }
 * ```
 */
interface Wander<S, T, A, B> {
  /** Visits every focus `A` of `s` with the effectful [f], rebuilding a `T` inside the applicative. */
  fun <F> run(applicative: Applicative<F>, f: (A) -> K1<F, B>, s: S): K1<F, T>
}
