package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.Selective

/**
 * The [Selective] instance every [Monad] has: [select] inspects the first result with `bind` and
 * runs the function effect only on `Left`. Mix it into any monad instance to get selective
 * combinators for free, e.g. `object: MonadSelective<Maybe.T>, MaybeMonad {}`.
 *
 * Law (in addition to the Selective laws): `select(fab, fn)` equals binding `fab` to
 * `fmap({ f -> f(a) }, fn)` on `Left(a)` and to `pure(b)` on `Right(b)`.
 *
 * @see Selective
 * @see Monad
 */
interface MonadSelective<T>: Selective<T>, Monad<T> {
  override fun <A, B> select(fab: K1<T, Either<A, B>>, fn: K1<T, (A) -> B>): K1<T, B> =
    bind({ e -> selectOne(e, fn) }, fab)

  private fun <A, B> selectOne(e: Either<A, B>, fn: K1<T, (A) -> B>): K1<T, B> = when (e) {
    is Either.Left -> fmap({ f -> f(e.value) }, fn)
    is Either.Right -> pure(e.value)
  }
}
