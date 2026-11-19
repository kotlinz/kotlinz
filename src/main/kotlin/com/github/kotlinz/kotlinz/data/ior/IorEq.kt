package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [Ior]: same case and equivalent contents under [eqA] / [eqB]. */
interface IorEq<A, B>: Eq<Ior<A, B>> {
  val eqA: Eq<A>
  val eqB: Eq<B>

  override fun eqv(a: Ior<A, B>, b: Ior<A, B>): Boolean = when {
    a is Ior.Left && b is Ior.Left -> eqA.eqv(a.value, b.value)
    a is Ior.Right && b is Ior.Right -> eqB.eqv(a.value, b.value)
    a is Ior.Both && b is Ior.Both -> eqA.eqv(a.left, b.left) && eqB.eqv(a.right, b.right)
    else -> false
  }
}

/** Returns the [Eq] instance for [Ior] derived from the given element instances. */
fun <A, B> Ior.Companion.eq(eqA: Eq<A>, eqB: Eq<B>): Eq<Ior<A, B>> = object: IorEq<A, B> { override val eqA = eqA; override val eqB = eqB }
