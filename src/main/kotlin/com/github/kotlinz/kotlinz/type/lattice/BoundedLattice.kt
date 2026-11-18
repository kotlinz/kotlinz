package com.github.kotlinz.kotlinz.type.lattice

/**
 * A [Lattice] with greatest and least elements.
 *
 * Laws:
 * - `join(a, bottom()) == a`
 * - `meet(a, top()) == a`
 *
 * @see Lattice
 */
interface BoundedLattice<A, Self: BoundedLattice<A, Self>>: Lattice<A, Self> {
  /** The greatest element: the identity of `meet`. */
  fun top(): Self
  /** The least element: the identity of `join`. */
  fun bottom(): Self

  companion object {
    /** Returns the greatest element of the lattice `type`. */
    fun <A: BoundedLattice<*, A>> top(type: Class<A>): A = type.getConstructor().newInstance().top()
    /** Returns the least element of the lattice `type`. */
    fun <A: BoundedLattice<*, A>> bottom(type: Class<A>): A = type.getConstructor().newInstance().bottom()
  }
}
