package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [Validated], rendered as `Valid(a)` or `Invalid(e)`. */
interface ValidatedShow<E, A>: Show<Validated<E, A>> {
  val showE: Show<E>
  val showA: Show<A>

  override fun show(a: Validated<E, A>): String = when (a) {
    is Validated.Valid -> "Valid(${showA.show(a.value)})"
    is Validated.Invalid -> "Invalid(${showE.show(a.error)})"
  }
}

/** Returns the [Show] instance for [Validated] derived from the given element instances. */
fun <E, A> Validated.Companion.show(showE: Show<E>, showA: Show<A>): Show<Validated<E, A>> = object: ValidatedShow<E, A> { override val showE = showE; override val showA = showA }
