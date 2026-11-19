package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [Tuple2], consistent with [Tuple2Eq]. */
interface Tuple2Hash<A, B>: Hash<Tuple2<A, B>>, Tuple2Eq<A, B> {
  val hashA: Hash<A>
  val hashB: Hash<B>
  override val eqA: Eq<A> get() = hashA
  override val eqB: Eq<B> get() = hashB

  override fun hash(a: Tuple2<A, B>): Int = Hash.combine(hashA.hash(a.first), hashB.hash(a.second))
}

/** Returns the [Hash] instance for [Tuple2] derived from the given element instances. */
fun <A, B> Tuple2.Companion.hash(hashA: Hash<A>, hashB: Hash<B>): Hash<Tuple2<A, B>> = object: Tuple2Hash<A, B> { override val hashA = hashA; override val hashB = hashB }
