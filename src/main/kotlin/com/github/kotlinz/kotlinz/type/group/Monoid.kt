package com.github.kotlinz.kotlinz.type.group

interface Monoid<A>: Semigroup<A> {
  fun mzero(): A
}
