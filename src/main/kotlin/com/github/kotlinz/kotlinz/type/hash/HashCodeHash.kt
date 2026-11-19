package com.github.kotlinz.kotlinz.type.hash

/** [Hash] instance that delegates to [Any.equals] and [Any.hashCode]. */
class HashCodeHash<A>: Hash<A> {
  override fun eqv(a: A, b: A): Boolean = a == b
  override fun hash(a: A): Int = a.hashCode()
}
