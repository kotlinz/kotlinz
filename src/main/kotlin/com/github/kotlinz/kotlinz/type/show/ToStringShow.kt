package com.github.kotlinz.kotlinz.type.show

/** [Show] instance that delegates to [Any.toString]. */
class ToStringShow<A>: Show<A> {
  override fun show(a: A): String = a.toString()
}
