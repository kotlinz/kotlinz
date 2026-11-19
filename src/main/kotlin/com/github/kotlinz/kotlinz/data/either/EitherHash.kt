package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [Either], consistent with [EitherEq]. */
interface EitherHash<L, R>: Hash<Either<L, R>>, EitherEq<L, R> {
  val hashL: Hash<L>
  val hashR: Hash<R>
  override val eqL: Eq<L> get() = hashL
  override val eqR: Eq<R> get() = hashR

  override fun hash(a: Either<L, R>): Int = when (a) {
    is Either.Left -> Hash.combine(0, hashL.hash(a.value))
    is Either.Right -> Hash.combine(1, hashR.hash(a.value))
  }
}

/** Returns the [Hash] instance for [Either] derived from the given element instances. */
fun <L, R> Either.Companion.hash(hashL: Hash<L>, hashR: Hash<R>): Hash<Either<L, R>> = object: EitherHash<L, R> { override val hashL = hashL; override val hashR = hashR }
