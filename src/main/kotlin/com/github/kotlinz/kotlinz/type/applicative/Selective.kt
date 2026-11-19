package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either

/**
 * A selective applicative functor: an [Applicative] that can decide, based on the result of one
 * effect, whether to run a second one. It sits between Applicative (all effects are static) and
 * Monad (effects may be arbitrary functions of earlier results).
 *
 * [select] runs `fab`; on `Right(b)` it may skip `fn` and returns `b`, on `Left(a)` it runs `fn`
 * and applies it to `a`.
 *
 * Laws (with `either(f, g)` eliminating an [Either]):
 * - Identity: `select(x, pure(id)) == fmap(either(id, id), x)`
 * - Distributivity: `select(pure(x), y *> z) == select(pure(x), y) *> select(pure(x), z)`
 * - Associativity: `select(x, select(y, z)) == select(select(fmap(f, x), fmap(g, y)), fmap(h, z))`
 *   where `f = { it.fmap(Right) }`, `g = { y -> { a -> y.bimap({ Pair(it, a) }, { it(a) }) } }`
 *   and `h = { z -> { (x, y) -> z(x)(y) } }`
 *
 * @see Applicative
 * @see com.github.kotlinz.kotlinz.type.monad.MonadSelective
 */
interface Selective<T>: Applicative<T> {
  /** Runs `fab`, then runs `fn` only when it produced a `Left`, applying the function to that value. */
  fun <A, B> select(fab: K1<T, Either<A, B>>, fn: K1<T, (A) -> B>): K1<T, B>

  /** Runs `fl` on `Left` or `fr` on `Right`, never both. */
  fun <A, B, C> branch(fab: K1<T, Either<A, B>>, fl: K1<T, (A) -> C>, fr: K1<T, (B) -> C>): K1<T, C> =
    select(select(fmap(::nestRight, fab), fmap({ f -> { a: A -> Either.Right<B, C>(f(a)) } }, fl)), fr)

  /** Runs `t` when `cond` yields `true` and `e` otherwise. */
  fun <A> ifS(cond: K1<T, Boolean>, t: K1<T, A>, e: K1<T, A>): K1<T, A> =
    branch(fmap(::toEither, cond), fmap({ a -> { _: Unit -> a } }, t), fmap({ a -> { _: Unit -> a } }, e))

  /** Runs `act` only when `cond` yields `true`. */
  fun whenS(cond: K1<T, Boolean>, act: K1<T, Unit>): K1<T, Unit> = ifS(cond, act, pure(Unit))
}

private fun <A, B, C> nestRight(e: Either<A, B>): Either<A, Either<B, C>> = when (e) {
  is Either.Left -> Either.Left(e.value)
  is Either.Right -> Either.Right(Either.Left(e.value))
}

private fun toEither(b: Boolean): Either<Unit, Unit> = if (b) Either.Left(Unit) else Either.Right(Unit)
