package com.github.kotlinz.kotlinz.type.ring

interface Semiring<A, Self: Semiring<A, Self>> {
  fun plus(a: Self, b: Self): Self
  fun times(a: Self, b: Self): Self
  fun zero(): Self
  fun one(): Self

  companion object {
    fun <A: Semiring<*, A>> plus(type: Class<A>, a: A, b: A): A = instance(type).plus(a, b)
    fun <A: Semiring<*, A>> times(type: Class<A>, a: A, b: A): A = instance(type).times(a, b)
    fun <A: Semiring<*, A>> zero(type: Class<A>): A = instance(type).zero()
    fun <A: Semiring<*, A>> one(type: Class<A>): A = instance(type).one()

    private fun <A: Semiring<*, A>> instance(type: Class<A>): A = type.getConstructor().newInstance()
  }
}
