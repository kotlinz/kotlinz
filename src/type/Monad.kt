package type

import K1

interface Monad<µ>: Applicative<µ> {
  fun <A> join(v: K1<µ, K1<µ, A>>): K1<µ, A>
  fun <A, B> bind(f: (A) -> K1<µ, B>, v: K1<µ, A>): K1<µ, B> = join(fmap(f, v))
}

interface MonadOps<A>: Monad<A> {
  infix fun <B> liftM(f: (A) -> B): (Monad<A>) -> Monad<B>
  infix fun <B, C> liftM2(f: ((A) -> B) -> C): ((Monad<A>) -> Monad<B>) -> Monad<C>
  infix fun <B, C, D> liftM3(f: (((A) -> B) -> C) -> D): (((MonadOps<A>) -> Monad<B>) -> Monad<C>) -> Monad<D>
}

interface MonadZip<A>: Monad<A> {
  fun <B> mzip(m: Monad<B>): Monad<Pair<A, B>>
  fun <B, C> mzipWith(m: Monad<B>, f: ((A) -> B) -> C): Monad<C>
  fun <B> munzip(f : Monad<Pair<A, B>>): Pair<MonadZip<A>, B>
}

interface MonadPlus<A>: Monad<A> {
  val mzero: MonadPlus<A>
  fun mplus(m: MonadPlus<A>): MonadPlus<A>
}