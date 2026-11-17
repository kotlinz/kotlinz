package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

data class NonEmptyList<A>(val head: A, val tail: List<A>): K1<NonEmptyList.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): NonEmptyList<A> = v as NonEmptyList<A>

    // Monad
    fun <A> pure(v: A): NonEmptyList<A> = monad.pure(v)
    fun <A> join(v: K1<T, K1<T, A>>): NonEmptyList<A> = monad.join(v)
    private val monad = object: NonEmptyListMonad {}
    fun <A> fx(block: suspend MonadScope<T>.() -> A): NonEmptyList<A> = narrow(monad.fxReplay(block))

    // ApplicativeOps
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: NonEmptyListApplicativeOps {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: NonEmptyListMonadOps {}

    fun <A> fromList(list: List<A>): NonEmptyList<A>? = list.firstOrNull()?.let { NonEmptyList(it, list.drop(1)) }

    // Traversable
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)
    private val traversable = object: NonEmptyListTraversable {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): NonEmptyList<B> = monad.fmap(f, this)
  infix fun <B> ap(f: K1<T, (A) -> B>): NonEmptyList<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> K1<T, B>): NonEmptyList<B> = monad.bind(f, this)
  private val monad = object: NonEmptyListMonad {}

  // Foldable
  fun <B> foldl(initial: B, f: (B, A) -> B): B = foldable.foldl(initial, f, this)
  fun <B> foldr(initial: B, f: (A, B) -> B): B = foldable.foldr(initial, f, this)
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = foldable.foldMap(type, f, this)
  private val foldable = object: NonEmptyListFoldable {}

  // Traversable
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)
  private val traversable = object: NonEmptyListTraversable {}

  // Comonad
  fun extract(): A = comonad.extract(this)
  fun duplicate(): NonEmptyList<K1<T, A>> = comonad.duplicate(this)
  fun <B> extend(f: (K1<T, A>) -> B): NonEmptyList<B> = comonad.extend(f, this)
  private val comonad = object: NonEmptyListComonad {}

  fun toList(): List<A> = listOf(head) + tail
  operator fun plus(other: NonEmptyList<A>): NonEmptyList<A> = NonEmptyList(head, tail + other.toList())
}

fun <A> nonEmptyListOf(head: A, vararg tail: A): NonEmptyList<A> = NonEmptyList(head, tail.toList())
