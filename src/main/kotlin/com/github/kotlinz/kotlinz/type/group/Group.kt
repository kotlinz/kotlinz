package com.github.kotlinz.kotlinz.type.group

interface Group<A, Self: Group<A, Self>>: Monoid<A, Self> {
  fun inverse(a: Self): Self

  companion object {
    fun <A: Group<*, A>> inverse(type: Class<A>, a: A): A = type.getConstructor().newInstance().inverse(a)
  }
}
