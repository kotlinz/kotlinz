package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.category.Category

interface Arrow<T> : Category<T> {
    fun <A, B> arr(f: (A) -> B): K2<T, A, B>

    fun <A, B, C> first(f: K2<T, A, B>): K2<T, Pair<A, C>, Pair<B, C>>

    fun <A, B, C> second(f: K2<T, A, B>): K2<T, Pair<C, A>, Pair<C, B>> =
            compose(swap<B, C>(), compose(first<A, B, C>(f), swap<C, A>()))

    fun <X, Y> swap(): K2<T, Pair<X, Y>, Pair<Y, X>> =
            arr { pair -> Pair(pair.second, pair.first) }

    fun <A, B, C, D> split(f: K2<T, A, B>, g: K2<T, C, D>): K2<T, Pair<A,  C>, Pair<B, D>> =
            compose(second<C, D, B>(g), first<A, B, C>(f))

    fun <A, B, C>combine(fab: K2<T, A, B>, fac: K2<T, A, C>): K2<T, A, Pair<B, C>> =
            compose(split(fab, fac), arr { a: A -> Pair(a, a) })
}