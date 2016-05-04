package type.group

interface Monoid<A>: Semigroup<A> {
  fun mzero(): A
}
