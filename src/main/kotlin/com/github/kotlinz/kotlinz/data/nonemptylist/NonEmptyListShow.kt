package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [NonEmptyList], rendered as `NonEmptyList(a, b)`. */
interface NonEmptyListShow<A>: Show<NonEmptyList<A>> {
  val showA: Show<A>

  override fun show(a: NonEmptyList<A>): String = a.toList().joinToString(", ", "NonEmptyList(", ")") { showA.show(it) }
}

/** Returns the [Show] instance for [NonEmptyList] derived from the given element instances. */
fun <A> NonEmptyList.Companion.show(showA: Show<A>): Show<NonEmptyList<A>> = object: NonEmptyListShow<A> { override val showA = showA }
