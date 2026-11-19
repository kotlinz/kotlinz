package com.github.kotlinz.kotlinz.type.order

/** [Order] instance that delegates to [Comparable.compareTo]; lawful whenever `compareTo` is a total order. */
class ComparableOrder<A: Comparable<A>>: Order<A> {
  override fun compare(a: A, b: A): Int = a.compareTo(b)
}
