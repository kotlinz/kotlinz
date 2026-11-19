package com.github.kotlinz.kotlinz.type.eq

/** [Eq] instance that delegates to [Any.equals]; lawful whenever `equals` is. */
class EqualsEq<A>: Eq<A> {
  override fun eqv(a: A, b: A): Boolean = a == b
}
