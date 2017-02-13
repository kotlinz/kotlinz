package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Category

interface Arrow<T, F> : Category<T, F> {
    fun <A, B> arr(f: (A) -> B): K3<T, F, A, B>

    fun <A, B, C> first(f: K3<T, F, A, B>): K3<T, F, Pair<A, C>, Pair<B, C>>

    fun <A, B, C> second(f: K3<T, F, A, B>): K3<T, F, Pair<C, A>, Pair<C, B>> =
            compose(swap<B, C>(), compose(first<A, B, C>(f), swap<C, A>()))

    fun <X, Y> swap(): K3<T, F, Pair<X, Y>, Pair<Y, X>> =
            arr { pair -> Pair(pair.second, pair.first) }

    fun <A, B, C, D> split(f: K3<T, F, A, B>, g: K3<T, F, C, D>): K3<T, F, Pair<A,  C>, Pair<B, D>> =
            compose(second<C, D, B>(g), first<A, B, C>(f))

    fun <A, B, C>combine(f: K3<T, F, A, B>, g: K3<T, F, A, C>): K3<T, F, A, Pair<B, C>> =
            compose(split(f, g), arr { a: A -> Pair(a, a) })
}