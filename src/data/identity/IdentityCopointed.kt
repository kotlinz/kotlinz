package data.identity

import type.pointed.Copointed
import K1

interface IdentityCopointed: Copointed<Identity.µ> {
  override fun <A> extract(v: K1<Identity.µ, A>): A = Identity.narrow(v).value
}
