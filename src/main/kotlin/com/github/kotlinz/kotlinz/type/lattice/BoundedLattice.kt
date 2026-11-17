package com.github.kotlinz.kotlinz.type.lattice

interface BoundedLattice<A, Self: BoundedLattice<A, Self>>: Lattice<A, Self> {
  fun top(): Self
  fun bottom(): Self

  companion object {
    fun <A: BoundedLattice<*, A>> top(type: Class<A>): A = type.getConstructor().newInstance().top()
    fun <A: BoundedLattice<*, A>> bottom(type: Class<A>): A = type.getConstructor().newInstance().bottom()
  }
}
