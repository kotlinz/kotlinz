package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.representable.Representable

/** Representable instance for [Identity], indexed by [Unit]. */
interface IdentityRepresentable: Representable<Identity.T, Unit>, IdentityFunctor {
  override fun <A> tabulate(f: (Unit) -> A): Identity<A> = Identity(f(Unit))
  override fun <A> index(fa: K1<Identity.T, A>): (Unit) -> A = { Identity.narrow(fa).value }
}
