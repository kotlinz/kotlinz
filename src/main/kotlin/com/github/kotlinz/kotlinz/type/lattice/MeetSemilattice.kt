package com.github.kotlinz.kotlinz.type.lattice

/**
 * A type with a greatest-lower-bound operation.
 *
 * Uses the same `Self` / no-arg constructor encoding as [com.github.kotlinz.kotlinz.type.group.Semigroup].
 *
 * Laws:
 * - Associativity: `meet(a, meet(b, c)) == meet(meet(a, b), c)`
 * - Commutativity: `meet(a, b) == meet(b, a)`
 * - Idempotence: `meet(a, a) == a`
 *
 * @see JoinSemilattice
 * @see Lattice
 */
interface MeetSemilattice<A, Self: MeetSemilattice<A, Self>> {
  /** Returns the greatest lower bound of `a` and `b`. */
  fun meet(a: Self, b: Self): Self

  companion object {
    /** Meets `a` and `b` using the instance created from `type`. */
    fun <A: MeetSemilattice<*, A>> meet(type: Class<A>, a: A, b: A): A = type.getConstructor().newInstance().meet(a, b)
  }
}
