package com.github.unhappychoice.kotlinz.type.group

interface Monoid<A>: Semigroup<A> {
  fun mzero(): A
}
