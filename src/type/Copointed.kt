package type

import K1

interface Copointed<µ> {
  fun <A> extract(v: K1<µ, A>): A
}
