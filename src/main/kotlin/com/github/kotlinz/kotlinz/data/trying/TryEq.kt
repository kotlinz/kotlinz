package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [Try]: successes are compared with [eqA] and failures with [eqError]. */
interface TryEq<A>: Eq<Try<A>> {
  val eqA: Eq<A>
  val eqError: Eq<Throwable>

  override fun eqv(a: Try<A>, b: Try<A>): Boolean = when {
    a is Try.Success && b is Try.Success -> eqA.eqv(a.value, b.value)
    a is Try.Failure && b is Try.Failure -> eqError.eqv(a.error, b.error)
    else -> false
  }
}

/** Returns the [Eq] instance for [Try]; errors are compared with `equals` unless [eqError] is given. */
fun <A> Try.Companion.eq(eqA: Eq<A>, eqError: Eq<Throwable> = Eq.fromEquals()): Eq<Try<A>> =
  object: TryEq<A> { override val eqA = eqA; override val eqError = eqError }
