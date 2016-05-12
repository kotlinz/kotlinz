package com.github.unhappychoice.kotlinz.data.identity

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.pointed.Copointed

interface IdentityCopointed: Copointed<Identity.T> {
  override fun <A> extract(v: K1<Identity.T, A>): A = Identity.narrow(v).value
}
