package type

import K1

interface Applicative<µ>: Functor<µ>, Pointed<µ> {
  fun <A, B> ap(f: K1<µ, (A) -> B>, v: K1<µ, A>): K1<µ, B>
}

interface ApplicativeOps<A>: Applicative<A> {
  infix fun <B> liftA(f: (A) -> B): (Applicative<A>) -> Applicative<B>
  infix fun <B, C> liftA2(f: ((A) -> B) -> C): ((Applicative<A>) -> Applicative<B>) -> Applicative<C>
  infix fun <B, C, D> liftA3(f: (((A) -> B) -> C) -> D): (((Applicative<A>) -> Applicative<B>) -> Applicative<C>) -> Applicative<D>
}