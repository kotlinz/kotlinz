package com.github.kotlinz.kotlinz.data.tagged

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * A value of type [B] tagged with a phantom input type [A], viewed as a profunctor.
 *
 * Running an optic on a `Tagged` builds the whole from a focus: it is the profunctor behind
 * `reverseGet`. Encoded as K3 with a phantom `Unit`, like `Function1`.
 *
 * Instances: [TaggedProfunctor], [TaggedChoice].
 *
 * ```kotlin
 * val choice = object : TaggedChoice {}
 * Tagged.narrow(choice.left<Int, Int, String>(Tagged(1))).value // Left(1)
 * ```
 *
 * @property value the carried value.
 */
data class Tagged<A, B>(val value: B): K3<Tagged.T, Unit, A, B> {
  /** Witness type used to encode `Tagged` as a higher-kinded `K3<Tagged.T, Unit, A, B>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [Tagged]. */
    fun <A, B> narrow(v: K1<K2<T, Unit, A>, B>): Tagged<A, B> = v as Tagged<A, B>
  }

  /** Changes the phantom input type. */
  fun <C> retag(): Tagged<C, B> = Tagged(value)

  /** Maps the carried value. */
  infix fun <C> fmap(f: (B) -> C): Tagged<A, C> = Tagged(f(value))
}
