package type

import K1

interface CoPointed<µ> {
  fun <A> extract(v: K1<µ, A>): A
}
