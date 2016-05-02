package type.monad

import K1

interface MonadPlus<µ>: Monad<µ> {
  fun <A> mzero(): K1<µ, A>
  fun <A> mplus(m: K1<µ, A>): K1<µ, A>
}
