package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * Encoded as K3 with a phantom `Unit` so that it fits `Category<T, F>` / `Arrow<T, F>`.
 */
class Function1<A, B>(val run: (A) -> B): K3<Function1.T, Unit, A, B> {
  class T

  companion object {
    fun <A, B> narrow(v: K1<K2<T, Unit, A>, B>): Function1<A, B> = v as Function1<A, B>

    // Monad
    fun <R, A> pure(v: A): Function1<R, A> = monad<R>().pure(v)
    fun <R, A> join(v: K1<K2<T, Unit, R>, K1<K2<T, Unit, R>, A>>): Function1<R, A> = monad<R>().join(v)
    private fun <R> monad() = object: Function1Monad<R> {}
    fun <R, A> fx(block: suspend MonadScope<K2<T, Unit, R>>.() -> A): Function1<R, A> = narrow(monad<R>().fx(block))

    // ApplicativeOps
    fun <R, A, B> liftA(f: (A) -> B) = applicativeOps<R>().liftA(f)
    fun <R, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<R>().liftA2(f)
    fun <R, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<R>().liftA3(f)
    private fun <R> applicativeOps() = object: Function1ApplicativeOps<R> {}

    // MonadOps
    fun <R, A, B> liftM(f: (A) -> B) = monadOps<R>().liftM(f)
    fun <R, A, B, C> liftM2(f: (A, B) -> C) = monadOps<R>().liftM2(f)
    fun <R, A, B, C, D> liftM3(f: (A, B, C) -> D) = monadOps<R>().liftM3(f)
    private fun <R> monadOps() = object: Function1MonadOps<R> {}

    // Arrow
    fun <A> id(): Function1<A, A> = arrow.id()
    fun <A, B> arr(f: (A) -> B): Function1<A, B> = arrow.arr(f)
    private val arrow = object: Function1Arrow {}
  }

  operator fun invoke(a: A): B = run(a)

  // Monad
  infix fun <C> fmap(f: (B) -> C): Function1<A, C> = monad.fmap(f, this)
  infix fun <C> ap(f: Function1<A, (B) -> C>): Function1<A, C> = monad.ap(f, this)
  infix fun <C> bind(f: (B) -> Function1<A, C>): Function1<A, C> = monad.bind(f, this)
  private val monad = object: Function1Monad<A> {}

  // Arrow
  infix fun <C> compose(g: Function1<C, A>): Function1<C, B> = arrow.compose(this, g)
  infix fun <C> andThen(g: Function1<B, C>): Function1<A, C> = arrow.compose(g, this)
  fun <C> first(): Function1<Pair<A, C>, Pair<B, C>> = arrow.first(this)
  fun <C> second(): Function1<Pair<C, A>, Pair<C, B>> = narrow(arrow.second(this))
  infix fun <C, D> split(g: Function1<C, D>): Function1<Pair<A, C>, Pair<B, D>> = narrow(arrow.split(this, g))
  infix fun <C> combine(g: Function1<A, C>): Function1<A, Pair<B, C>> = narrow(arrow.combine(this, g))
  private val arrow = object: Function1Arrow {}

  // Profunctor
  fun <C, D> dimap(f: (C) -> A, g: (B) -> D): Function1<C, D> = profunctor.dimap(f, g, this)
  infix fun <C> lmap(f: (C) -> A): Function1<C, B> = profunctor.lmap(f, this)
  infix fun <C> rmap(g: (B) -> C): Function1<A, C> = profunctor.rmap(g, this)
  private val profunctor = object: Function1Profunctor {}
}
