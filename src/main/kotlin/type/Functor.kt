package type

import K1

interface Functor<µ> {
  fun <A, B> fmap(f: (A) -> B, v: K1<µ, A>): K1<µ, B>
}