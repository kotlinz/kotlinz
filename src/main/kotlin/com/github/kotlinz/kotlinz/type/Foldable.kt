package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid

interface Foldable<T> {
  fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<T, A>): B
  fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<T, A>): B

  fun <A, M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M, v: K1<T, A>): M =
    foldl(Monoid.mzero(type), { acc: M, a: A -> Monoid.op(type, acc, f(a)) }, v)

  fun <A> toList(v: K1<T, A>): List<A> = foldr(listOf(), { a: A, acc: List<A> -> listOf(a) + acc }, v)
  fun <A> length(v: K1<T, A>): Int = foldl(0, { acc: Int, _: A -> acc + 1 }, v)
  fun <A> isEmpty(v: K1<T, A>): Boolean = length(v) == 0
}
