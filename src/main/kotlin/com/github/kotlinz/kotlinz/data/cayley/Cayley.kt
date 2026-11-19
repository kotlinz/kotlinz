package com.github.kotlinz.kotlinz.data.cayley

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * A profunctor `P<A, B>` inside a functor `F`: `F<P<A, B>>`.
 *
 * `P` is identified by the tags `PT, PF` (values `K3<PT, PF, _, _>`); `F` and those tags are
 * bundled into the phantom [Of] so that `Cayley` fits `Profunctor<T, F>`.
 *
 * Instances: [CayleyProfunctor], [CayleyStrong] (need `Functor<F>` and the matching instance of `P`).
 *
 * ```kotlin
 * val profunctor = object : CayleyProfunctor<ListK.T, Function1.T, Unit> {
 *   override val functor = object : ListKFunctor {}
 *   override val profunctor = object : Function1Profunctor {}
 * }
 * val fs = Cayley(listKOf<K3<Function1.T, Unit, Int, Int>>(Function1 { it + 1 }, Function1 { it * 2 }))
 * ListK.narrow(profunctor.rmap({ n: Int -> "$n" }, fs).value).value.map { Function1.narrow(it)(3) } // ["4", "6"]
 * ```
 *
 * @property value the wrapped functor of profunctor values.
 */
class Cayley<F, PT, PF, A, B>(val value: K1<F, K3<PT, PF, A, B>>): K3<Cayley.T, Cayley.Of<F, PT, PF>, A, B> {
  class T
  /** Phantom bundling the functor and the profunctor tags. */
  class Of<F, PT, PF>

  companion object {
    /** Downcasts a higher-kinded value back to [Cayley]. */
    fun <F, PT, PF, A, B> narrow(v: K1<K2<T, Of<F, PT, PF>, A>, B>): Cayley<F, PT, PF, A, B> = v as Cayley<F, PT, PF, A, B>
  }
}
