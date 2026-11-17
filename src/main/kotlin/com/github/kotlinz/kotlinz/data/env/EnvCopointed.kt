package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

interface EnvCopointed<E>: Copointed<K1<Env.T, E>> {
  override fun <A> extract(v: K1<K1<Env.T, E>, A>): A = Env.narrow(v).value
}
