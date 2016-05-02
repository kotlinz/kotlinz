package type.monad

import K1

interface MonadOps<µ>: Monad<µ> {
  fun <A, B> liftM(f: (A) -> B): (K1<µ, A>) -> K1<µ, B>
  fun <A, B, C> liftM2(f: ((A) -> B) -> C): ((K1<µ, A>) -> K1<µ, B>) -> K1<µ, C>
  fun <A, B, C, D> liftM3(f: (((A) -> B) -> C) -> D): (((K1<µ, A>) -> K1<µ, B>) -> K1<µ, C>) -> K1<µ, D>
}
