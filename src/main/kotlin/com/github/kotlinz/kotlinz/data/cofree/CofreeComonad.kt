package com.github.kotlinz.kotlinz.data.cofree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [Cofree]. Requires a Functor of F. */
interface CofreeComonad<F>: Comonad<K1<Cofree.T, F>>, CofreeCopointed<F>, CofreeFunctor<F> {
  companion object {
    /** Creates the instance from a Functor of F. */
    fun <F> of(functor: Functor<F>): CofreeComonad<F> {
      val base = functor
      return object: CofreeComonad<F> {
        override val functor = base
      }
    }
  }

  override fun <A> duplicate(v: K1<K1<Cofree.T, F>, A>): Cofree<F, K1<K1<Cofree.T, F>, A>> = extend({ it }, v)

  override fun <A, B> extend(f: (K1<K1<Cofree.T, F>, A>) -> B, v: K1<K1<Cofree.T, F>, A>): Cofree<F, B> {
    val cofree = Cofree.narrow(v)
    return Cofree(f(cofree)) { functor.fmap({ c: Cofree<F, A> -> extend(f, c) }, cofree.tail) }
  }
}
