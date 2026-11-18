package com.github.kotlinz.kotlinz.type.group

/**
 * A [Semigroup] whose operation is idempotent.
 *
 * Law:
 * - Idempotence: `op(a, a) == a`
 *
 * @see Semigroup
 * @see com.github.kotlinz.kotlinz.type.lattice.JoinSemilattice
 */
interface Band<A, Self: Band<A, Self>>: Semigroup<A, Self>
