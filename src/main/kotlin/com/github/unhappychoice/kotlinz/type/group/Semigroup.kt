package com.github.unhappychoice.kotlinz.type.group

interface Semigroup<A> {
  fun op(a: A, b: A): A
}
