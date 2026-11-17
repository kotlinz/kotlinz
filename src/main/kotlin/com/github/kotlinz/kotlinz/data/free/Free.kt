package com.github.kotlinz.kotlinz.data.free

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

sealed class Free<F, A>: K2<Free.T, F, A> {
  class T

  class Pure<F, A>(val value: A): Free<F, A>()
  class Suspend<F, A>(val fa: K1<F, A>): Free<F, A>()
  class FlatMap<F, X, A>(val sub: Free<F, X>, val f: (X) -> Free<F, A>): Free<F, A>()

  companion object {
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Free<F, A> = v as Free<F, A>

    fun <F, A> liftF(fa: K1<F, A>): Free<F, A> = Suspend(fa)

    // Monad
    fun <F, A> pure(v: A): Free<F, A> = monad<F>().pure(v)
    fun <F, A> join(v: K1<K1<T, F>, K1<K1<T, F>, A>>): Free<F, A> = monad<F>().join(v)
    private fun <F> monad() = object: FreeMonad<F> {}

    // Re-runs the block per bound value so that any interpreter, including multi-shot ones, is supported
    fun <F, A> fx(block: suspend MonadScope<K1<T, F>>.() -> A): Free<F, A> = narrow(monad<F>().fxReplay(block))
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): Free<F, B> = monad.fmap(f, this)
  infix fun <B> ap(f: Free<F, (A) -> B>): Free<F, B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> Free<F, B>): Free<F, B> = monad.bind(f, this)
  private val monad get() = object: FreeMonad<F> {}

  fun <M> foldMap(nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> = FreeInterpreter.foldMap(this, nt, monad)
}
