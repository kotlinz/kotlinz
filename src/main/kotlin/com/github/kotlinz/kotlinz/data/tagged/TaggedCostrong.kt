package com.github.kotlinz.kotlinz.data.tagged

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Costrong

/** Costrong instance for [Tagged]: keeps only the wanted component of the carried pair. */
interface TaggedCostrong: Costrong<Tagged.T, Unit>, TaggedProfunctor {
  override fun <A, B, C> unfirst(v: K3<Tagged.T, Unit, Pair<A, C>, Pair<B, C>>): Tagged<A, B> =
    Tagged(Tagged.narrow(v).value.first)

  override fun <A, B, C> unsecond(v: K3<Tagged.T, Unit, Pair<C, A>, Pair<C, B>>): Tagged<A, B> =
    Tagged(Tagged.narrow(v).value.second)
}
