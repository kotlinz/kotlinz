package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [Ior], consistent with [IorEq]. */
interface IorHash<A, B>: Hash<Ior<A, B>>, IorEq<A, B> {
  val hashA: Hash<A>
  val hashB: Hash<B>
  override val eqA: Eq<A> get() = hashA
  override val eqB: Eq<B> get() = hashB

  override fun hash(a: Ior<A, B>): Int = when (a) {
    is Ior.Left -> Hash.combine(0, hashA.hash(a.value))
    is Ior.Right -> Hash.combine(1, hashB.hash(a.value))
    is Ior.Both -> Hash.combine(2, hashA.hash(a.left), hashB.hash(a.right))
  }
}

/** Returns the [Hash] instance for [Ior] derived from the given element instances. */
fun <A, B> Ior.Companion.hash(hashA: Hash<A>, hashB: Hash<B>): Hash<Ior<A, B>> = object: IorHash<A, B> { override val hashA = hashA; override val hashB = hashB }
