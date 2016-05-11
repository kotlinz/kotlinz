package type.pointed

import K1

interface Pointed<µ> {
  fun <A> pure(v: A): K1<µ, A>
}
