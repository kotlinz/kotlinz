package com.github.kotlinz.kotlinz.data.twin

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.representable.Representable

interface TwinRepresentable: Representable<Twin.T, Boolean>, TwinFunctor {
  override fun <A> tabulate(f: (Boolean) -> A): Twin<A> = Twin(f(false), f(true))

  override fun <A> index(fa: K1<Twin.T, A>): (Boolean) -> A {
    val twin = Twin.narrow(fa)
    return { rep -> if (rep) twin.second else twin.first }
  }
}
