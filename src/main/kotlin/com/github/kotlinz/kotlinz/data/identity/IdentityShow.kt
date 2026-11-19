package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [Identity], rendered as `Identity(a)`. */
interface IdentityShow<A>: Show<Identity<A>> {
  val showA: Show<A>

  override fun show(a: Identity<A>): String = "Identity(${showA.show(a.value)})"
}

/** Returns the [Show] instance for [Identity] derived from the given element instances. */
fun <A> Identity.Companion.show(showA: Show<A>): Show<Identity<A>> = object: IdentityShow<A> { override val showA = showA }
