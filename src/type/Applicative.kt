package type

import K1

interface Applicative<µ>: Functor<µ>, Pointed<µ> {
  fun <A, B> ap(f: K1<µ, (A) -> B>, v: K1<µ, A>): K1<µ, B>
}

interface ApplicativeOps<µ>: Applicative<µ> {
  fun <A, B> liftA(f: (A) -> B): (K1<µ, A>) -> K1<µ, A>
  fun <A, B, C> liftA2(f: ((A) -> B) -> C): ((K1<µ, A>) -> K1<µ, B>) -> K1<µ, C>
  fun <A, B, C, D> liftA3(f: (((A) -> B) -> C) -> D): (((K1<µ, A>) -> K1<µ, B>) -> K1<µ, C>) -> K1<µ, D>
}