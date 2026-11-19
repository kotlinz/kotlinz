package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [Try], rendered as `Success(a)` or `Failure(error)`. */
interface TryShow<A>: Show<Try<A>> {
  val showA: Show<A>
  val showError: Show<Throwable>

  override fun show(a: Try<A>): String = when (a) {
    is Try.Success -> "Success(${showA.show(a.value)})"
    is Try.Failure -> "Failure(${showError.show(a.error)})"
  }
}

/** Returns the [Show] instance for [Try]; errors are rendered with `toString` unless [showError] is given. */
fun <A> Try.Companion.show(showA: Show<A>, showError: Show<Throwable> = Show.fromToString()): Show<Try<A>> =
  object: TryShow<A> { override val showA = showA; override val showError = showError }
