package com.github.kotlinz.kotlinz.type.group

interface Monoid<A, Self: Monoid<A, Self>>: Semigroup<A, Self> {
  fun mzero(): Self

  companion object {
    fun <A: Monoid<*, A>> op(type: Class<A>, a: A, b: A): A = type.getConstructor().newInstance().op(a, b)
    fun <A: Monoid<*, A>> mzero(type: Class<A>): A = type.getConstructor().newInstance().mzero()
  }
}
