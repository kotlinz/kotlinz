package type

import K1
import type.group.Monoid

interface Foldable<µ> {
  fun <A, B> foldl(initial: A, f: ((A) -> B) -> A, v: K1<µ, A>): A
  fun <A, B> foldr(initial: B, f: ((A) -> A) -> B, v: K1<µ, B>): B
  fun <A, M: Monoid<µ>> foldMap(f: (A) -> M, v: K1<µ, A>): M
}