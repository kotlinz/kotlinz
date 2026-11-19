package com.github.kotlinz.kotlinz.data.forget

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * A function `(A) -> R` that ignores its output type [B], viewed as a profunctor.
 *
 * Running an optic on a `Forget` extracts the focus: it is the profunctor behind `get`, `getMaybe`
 * and folds.
 *
 * Instances: [ForgetProfunctor], [ForgetStrong], [ForgetCochoice], [ForgetChoice] and [ForgetTraversing]
 * (need a monoid on [R]).
 *
 * ```kotlin
 * val length = Forget<Int, String, Nothing> { s -> s.length }
 * val strong = object : ForgetStrong<Int> {}
 * Forget.narrow(strong.first<String, Nothing, Boolean>(length))(Pair("abc", true)) // 3
 * ```
 *
 * @property run the underlying function.
 */
class Forget<R, A, B>(val run: (A) -> R): K3<Forget.T, R, A, B> {
  /** Witness type used to encode `Forget` as a higher-kinded `K3<Forget.T, R, A, B>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [Forget]. */
    fun <R, A, B> narrow(v: K1<K2<T, R, A>, B>): Forget<R, A, B> = v as Forget<R, A, B>
  }

  /** Applies the function to [a]. */
  operator fun invoke(a: A): R = run(a)

  /** Changes the phantom output type. */
  fun <C> retag(): Forget<R, A, C> = Forget(run)
}
