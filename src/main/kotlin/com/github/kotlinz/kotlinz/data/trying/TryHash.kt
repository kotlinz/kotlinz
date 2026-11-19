package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [Try], consistent with [TryEq]. */
interface TryHash<A>: Hash<Try<A>>, TryEq<A> {
  val hashA: Hash<A>
  val hashError: Hash<Throwable>
  override val eqA: Eq<A> get() = hashA
  override val eqError: Eq<Throwable> get() = hashError

  override fun hash(a: Try<A>): Int = when (a) {
    is Try.Success -> Hash.combine(0, hashA.hash(a.value))
    is Try.Failure -> Hash.combine(1, hashError.hash(a.error))
  }
}

/** Returns the [Hash] instance for [Try]; errors use `equals` / `hashCode` unless [hashError] is given. */
fun <A> Try.Companion.hash(hashA: Hash<A>, hashError: Hash<Throwable> = Hash.fromHashCode()): Hash<Try<A>> =
  object: TryHash<A> { override val hashA = hashA; override val hashError = hashError }
