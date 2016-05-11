package type.monad

import K1
import type.applicative.Applicative

interface Monad<µ>: Applicative<µ> {
  fun <A> join(v: K1<µ, K1<µ, A>>): K1<µ, A>
  fun <A, B> bind(f: (A) -> K1<µ, B>, v: K1<µ, A>): K1<µ, B> = join(fmap(f, v))
}
