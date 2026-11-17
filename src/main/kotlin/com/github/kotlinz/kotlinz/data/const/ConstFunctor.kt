package com.github.kotlinz.kotlinz.data.const

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface ConstFunctor<S>: Functor<K1<Const.T, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Const.T, S>, A>): Const<S, B> = Const.narrow(v).retag()
}
