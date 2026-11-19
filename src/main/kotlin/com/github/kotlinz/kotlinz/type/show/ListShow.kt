package com.github.kotlinz.kotlinz.type.show

/** [Show] instance for [List], rendered as `[a, b, c]` using [showA]. */
interface ListShow<A>: Show<List<A>> {
  val showA: Show<A>

  override fun show(a: List<A>): String = a.joinToString(", ", "[", "]") { showA.show(it) }
}
