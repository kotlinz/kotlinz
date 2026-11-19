package com.github.kotlinz.kotlinz.data.freeselective

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.Selective

/** Selective instance for [FreeSelective]; `ap` is derived from `select`, running the function's effects first. */
interface FreeSelectiveSelective<F>: Selective<K1<FreeSelective.T, F>>, FreeSelectiveFunctor<F> {
  override fun <A> pure(v: A): FreeSelective<F, A> = FreeSelective.Pure(v)

  override fun <A, B> select(fab: K1<K1<FreeSelective.T, F>, Either<A, B>>, fn: K1<K1<FreeSelective.T, F>, (A) -> B>): FreeSelective<F, B> =
    FreeSelective.narrow(fn).selectWith(FreeSelective.narrow(fab)) { it }

  override fun <A, B> ap(f: K1<K1<FreeSelective.T, F>, (A) -> B>, v: K1<K1<FreeSelective.T, F>, A>): FreeSelective<F, B> {
    val left = FreeSelective.narrow(f).fmap<Either<(A) -> B, B>> { g -> Either.Left(g) }
    return select(left, FreeSelective.narrow(v).fmap { a -> { g: (A) -> B -> g(a) } })
  }
}
