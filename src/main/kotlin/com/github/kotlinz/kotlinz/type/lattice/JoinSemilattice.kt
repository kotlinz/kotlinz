package com.github.kotlinz.kotlinz.type.lattice

interface JoinSemilattice<A, Self: JoinSemilattice<A, Self>> {
  fun join(a: Self, b: Self): Self

  companion object {
    fun <A: JoinSemilattice<*, A>> join(type: Class<A>, a: A, b: A): A = type.getConstructor().newInstance().join(a, b)
  }
}
