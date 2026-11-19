package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable1

/** Foldable1 instance for [Tuple2], folding over its single second component. */
interface Tuple2Foldable1<S>: Foldable1<K1<Tuple2.T, S>> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<K1<Tuple2.T, S>, A>): B = f(initial, Tuple2.narrow(v).second)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<K1<Tuple2.T, S>, A>): B = f(Tuple2.narrow(v).second, initial)
  override fun <A> reduceLeft(f: (A, A) -> A, v: K1<K1<Tuple2.T, S>, A>): A = Tuple2.narrow(v).second
  override fun <A> reduceRight(f: (A, A) -> A, v: K1<K1<Tuple2.T, S>, A>): A = Tuple2.narrow(v).second
}
