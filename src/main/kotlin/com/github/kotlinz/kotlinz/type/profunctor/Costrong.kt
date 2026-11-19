package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3

/**
 * The dual of [Strong]: a profunctor that can drop a component of a pair threaded through it.
 *
 * Instances for functions need lazy feedback (`fix`), so only strict-friendly profunctors such as
 * `Tagged` provide one.
 *
 * Laws:
 * - `unfirst(v) == unsecond(dimap(Pair::swap, Pair::swap, v))`
 * - `unfirst(unfirst(v)) == unfirst(dimap(unassoc, assoc, v))`
 * - For a profunctor that is also [Strong]: `unfirst(first(v)) == v`
 *
 * ```kotlin
 * val costrong = object : TaggedCostrong {}
 * costrong.unfirst(Tagged<Pair<Int, String>, Pair<Int, String>>(Pair(1, "x"))).value // 1
 * ```
 *
 * @see Strong
 */
interface Costrong<T, F> : Profunctor<T, F> {
  /** Removes the second component carried alongside `A` / `B`. */
  fun <A, B, C> unfirst(v: K3<T, F, Pair<A, C>, Pair<B, C>>): K3<T, F, A, B>

  /** Removes the first component carried alongside `A` / `B`. */
  fun <A, B, C> unsecond(v: K3<T, F, Pair<C, A>, Pair<C, B>>): K3<T, F, A, B> =
    unfirst<A, B, C>(dimap({ p: Pair<A, C> -> Pair(p.second, p.first) }, { p: Pair<C, B> -> Pair(p.second, p.first) }, v))
}
