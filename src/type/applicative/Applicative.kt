package type.applicative

import K1
import type.Functor
import type.Pointed

interface Applicative<µ>: Functor<µ>, Pointed<µ> {
  fun <A, B> ap(f: K1<µ, (A) -> B>, v: K1<µ, A>): K1<µ, B>
}