package com.github.kotlinz.kotlinz.type.lattice

/**
 * A type that is both a join- and a meet-semilattice.
 *
 * Laws (absorption):
 * - `join(a, meet(a, b)) == a`
 * - `meet(a, join(a, b)) == a`
 *
 * @see JoinSemilattice
 * @see MeetSemilattice
 * @see BoundedLattice
 */
interface Lattice<A, Self: Lattice<A, Self>>: JoinSemilattice<A, Self>, MeetSemilattice<A, Self>
