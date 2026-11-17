package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3

interface Strong<T, F> : Profunctor<T, F> {
  fun <A, B, C> first(v: K3<T, F, A, B>): K3<T, F, Pair<A, C>, Pair<B, C>>

  fun <A, B, C> second(v: K3<T, F, A, B>): K3<T, F, Pair<C, A>, Pair<C, B>> =
    dimap({ p: Pair<C, A> -> p.swap() }, { p: Pair<B, C> -> p.swap() }, first<A, B, C>(v))
}

private fun <X, Y> Pair<X, Y>.swap(): Pair<Y, X> = Pair(second, first)
