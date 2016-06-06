package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

interface IdentityCopointed: Copointed<Identity.T> {
  override fun <A> extract(v: K1<Identity.T, A>): A = Identity.narrow(v).value
}
