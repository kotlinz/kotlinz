package com.github.kotlinz.kotlinz.type.lattice

/**
 * A type with a least-upper-bound operation.
 *
 * Uses the same `Self` / no-arg constructor encoding as [com.github.kotlinz.kotlinz.type.group.Semigroup].
 *
 * Laws:
 * - Associativity: `join(a, join(b, c)) == join(join(a, b), c)`
 * - Commutativity: `join(a, b) == join(b, a)`
 * - Idempotence: `join(a, a) == a`
 *
 * @see MeetSemilattice
 * @see Lattice
 */
interface JoinSemilattice<A, Self: JoinSemilattice<A, Self>> {
  /** Returns the least upper bound of `a` and `b`. */
  fun join(a: Self, b: Self): Self

  companion object {
    /** Joins `a` and `b` using the instance created from `type`. */
    fun <A: JoinSemilattice<*, A>> join(type: Class<A>, a: A, b: A): A = type.getConstructor().newInstance().join(a, b)
  }
}
