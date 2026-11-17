package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

data class ListK<A>(val value: List<A>): K1<ListK.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): ListK<A> = v as ListK<A>

    // Monad
    fun <A> pure(v: A): ListK<A> = monad.pure(v)
    fun <A> join(v: K1<T, K1<T, A>>): ListK<A> = monad.join(v)
    private val monad = object: ListKMonad {}
    fun <A> fx(block: suspend MonadScope<T>.() -> A): ListK<A> = narrow(monad.fxReplay(block))

    // ApplicativeOps
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: ListKApplicativeOps {}

    // Alternative
    fun <A> empty(): ListK<A> = alternative.empty()
    private val alternative = object: ListKAlternative {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: ListKMonadOps {}

    // MonadPlus
    fun <A> mzero(): ListK<A> = monadPlus.mzero()
    private val monadPlus = object: ListKMonadPlus {}

    // MonadZip
    fun <A, B> munzip(m: K1<T, Pair<A, B>>): Pair<ListK<A>, ListK<B>> = monadZip.munzip(m)
    private val monadZip = object: ListKMonadZip {}

    // Traversable
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)
    private val traversable = object: ListKTraversable {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): ListK<B> = monad.fmap(f, this)
  infix fun <B> ap(f: K1<T, (A) -> B>): ListK<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> K1<T, B>): ListK<B> = monad.bind(f, this)
  private val monad = object: ListKMonad {}

  // Foldable
  fun <B> foldl(initial: B, f: (B, A) -> B): B = foldable.foldl(initial, f, this)
  fun <B> foldr(initial: B, f: (A, B) -> B): B = foldable.foldr(initial, f, this)
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = foldable.foldMap(type, f, this)
  private val foldable = object: ListKFoldable {}

  // Traversable
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)
  private val traversable = object: ListKTraversable {}

  // MonadPlus
  infix fun mplus(m: K1<T, A>): ListK<A> = monadPlus.mplus(this, m)
  private val monadPlus = object: ListKMonadPlus {}

  // Alternative
  infix fun orElse(m: K1<T, A>): ListK<A> = alternative.orElse(this, m)
  fun optional(): ListK<Maybe<A>> = narrow(alternative.optional(this))
  private val alternative = object: ListKAlternative {}

  // MonadZip
  infix fun <B> mzip(m: K1<T, B>): ListK<Pair<A, B>> = monadZip.mzip(this, m)
  fun <B, C> mzipWith(m: K1<T, B>, f: (A, B) -> C): ListK<C> = monadZip.mzipWith(this, m, f)
  private val monadZip = object: ListKMonadZip {}
}

fun <A> List<A>.toListK(): ListK<A> = ListK(this)
fun <A> listKOf(vararg values: A): ListK<A> = ListK(values.toList())
