package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [Maybe], rendered as `Just(a)` or `None`. */
interface MaybeShow<A>: Show<Maybe<A>> {
  val showA: Show<A>

  override fun show(a: Maybe<A>): String = when (a) {
    is Maybe.Just -> "Just(${showA.show(a.value)})"
    is Maybe.None -> "None"
  }
}

/** Returns the [Show] instance for [Maybe] derived from the given element instances. */
fun <A> Maybe.Companion.show(showA: Show<A>): Show<Maybe<A>> = object: MaybeShow<A> { override val showA = showA }
