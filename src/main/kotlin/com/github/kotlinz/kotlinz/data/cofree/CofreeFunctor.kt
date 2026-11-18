package com.github.kotlinz.kotlinz.data.cofree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Cofree]. Requires a Functor of F; the tail is mapped lazily. */
interface CofreeFunctor<F>: Functor<K1<Cofree.T, F>> {
  /** Functor of the branching structure F. */
  val functor: Functor<F>

  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Cofree.T, F>, A>): Cofree<F, B> {
    val cofree = Cofree.narrow(v)
    return Cofree(f(cofree.head)) { functor.fmap({ c: Cofree<F, A> -> fmap(f, c) }, cofree.tail) }
  }
}
