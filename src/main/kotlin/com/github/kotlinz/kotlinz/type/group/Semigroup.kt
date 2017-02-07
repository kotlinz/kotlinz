package com.github.kotlinz.kotlinz.type.group

interface Semigroup<A, Self: Semigroup<A, Self>> {
  fun op(a: Self, b: Self): Self

  companion object {
    fun <A: Semigroup<*, A>> op(type: Class<A>, a: A, b: A): A = type.newInstance().op(a, b)
  }
}
