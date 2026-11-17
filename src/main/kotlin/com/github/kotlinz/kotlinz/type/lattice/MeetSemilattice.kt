package com.github.kotlinz.kotlinz.type.lattice

interface MeetSemilattice<A, Self: MeetSemilattice<A, Self>> {
  fun meet(a: Self, b: Self): Self

  companion object {
    fun <A: MeetSemilattice<*, A>> meet(type: Class<A>, a: A, b: A): A = type.getConstructor().newInstance().meet(a, b)
  }
}
