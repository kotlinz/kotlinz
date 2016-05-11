package com.github.unhappychoice.kotlinz.data.identity

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.pointed.Copointed

interface IdentityCopointed: Copointed<Identity.µ> {
  override fun <A> extract(v: K1<Identity.µ, A>): A = Identity.narrow(v).value
}
