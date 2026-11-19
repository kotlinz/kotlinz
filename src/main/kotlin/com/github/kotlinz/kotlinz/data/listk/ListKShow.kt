package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [ListK], rendered as `ListK([a, b])`. */
interface ListKShow<A>: Show<ListK<A>> {
  val showA: Show<A>

  override fun show(a: ListK<A>): String = "ListK(${Show.list(showA).show(a.value)})"
}

/** Returns the [Show] instance for [ListK] derived from the given element instances. */
fun <A> ListK.Companion.show(showA: Show<A>): Show<ListK<A>> = object: ListKShow<A> { override val showA = showA }
