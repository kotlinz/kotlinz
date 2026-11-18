package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Category

/**
 * A category that can lift plain functions and route pairs through arrows, generalising functions
 * to computations with effects or structure.
 *
 * Laws:
 * - `arr(id) == id()`
 * - `arr(g . f) == compose(arr(g), arr(f))`
 * - `first(arr(f)) == arr { (a, c) -> Pair(f(a), c) }`
 * - `first(compose(g, f)) == compose(first(g), first(f))`
 * - `compose(arr { it.first }, first(f)) == compose(f, arr { it.first })`
 *
 * @see com.github.kotlinz.kotlinz.type.category.Category
 * @see com.github.kotlinz.kotlinz.type.profunctor.Strong
 */
interface Arrow<T, F> : Category<T, F> {
    /** Lifts a plain function into an arrow. */
    fun <A, B> arr(f: (A) -> B): K3<T, F, A, B>

    /** Applies `f` to the first component of a pair, passing the second through unchanged. */
    fun <A, B, C> first(f: K3<T, F, A, B>): K3<T, F, Pair<A, C>, Pair<B, C>>

    /** Applies `f` to the second component of a pair, passing the first through unchanged. */
    fun <A, B, C> second(f: K3<T, F, A, B>): K3<T, F, Pair<C, A>, Pair<C, B>> =
            compose(swap<B, C>(), compose(first<A, B, C>(f), swap<C, A>()))

    /** The arrow that swaps the components of a pair. */
    fun <X, Y> swap(): K3<T, F, Pair<X, Y>, Pair<Y, X>> =
            arr { pair -> Pair(pair.second, pair.first) }

    /** Runs `f` on the first component and `g` on the second (`***`). */
    fun <A, B, C, D> split(f: K3<T, F, A, B>, g: K3<T, F, C, D>): K3<T, F, Pair<A,  C>, Pair<B, D>> =
            compose(second<C, D, B>(g), first<A, B, C>(f))

    /** Feeds the same input to `f` and `g` and pairs the results (`&&&`). */
    fun <A, B, C>combine(f: K3<T, F, A, B>, g: K3<T, F, A, C>): K3<T, F, A, Pair<B, C>> =
            compose(split(f, g), arr { a: A -> Pair(a, a) })
}