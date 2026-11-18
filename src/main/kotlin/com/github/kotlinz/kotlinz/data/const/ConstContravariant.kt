package com.github.kotlinz.kotlinz.data.const

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Contravariant

/** Contravariant instance for [Const]. */
interface ConstContravariant<S>: Contravariant<K1<Const.T, S>> {
  override fun <A, B> contramap(f: (B) -> A, v: K1<K1<Const.T, S>, A>): Const<S, B> = Const.narrow(v).retag()
}
