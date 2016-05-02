package type.monad

import K1

interface MonadZip<µ>: Monad<µ> {
  fun <A, B> mzip(m: K1<µ, B>): K1<µ, Pair<A, B>>
  fun <A, B, C> mzipWith(m: K1<µ, B>, f: ((A) -> B) -> C): K1<µ, C>
  fun <A, B> munzip(f : K1<µ, Pair<A, B>>): Pair<K1<µ, A>, B>
}
