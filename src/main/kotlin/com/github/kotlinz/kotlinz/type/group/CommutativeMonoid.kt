package com.github.kotlinz.kotlinz.type.group

/**
 * A [Monoid] whose operation is commutative, so the order of combination does not matter.
 *
 * This is a marker: it adds no operations, but lets APIs require that elements may be combined in any
 * order (e.g. when folding unordered collections or merging results in parallel).
 *
 * Law:
 * - Commutativity: `op(a, b) == op(b, a)`
 *
 * @see Monoid
 */
interface CommutativeMonoid<A, Self: CommutativeMonoid<A, Self>>: Monoid<A, Self>
