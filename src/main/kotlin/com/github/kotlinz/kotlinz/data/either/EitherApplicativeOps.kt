package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

interface EitherApplicativeOps<S>: ApplicativeOps<K1<Either.T, S>>, EitherMonad<S> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K1<Either.T, S>, A>) -> Either<S, B> {
    return { a -> Either.narrow(a) ap Either.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K1<Either.T, S>, A>, K1<K1<Either.T, S>, B>) -> Either<S, C> {
    return { a1, a2 ->
      val i1 = Either.narrow(a1)
      val i2 = Either.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K1<Either.T, S>, A>, K1<K1<Either.T, S>, B>, K1<K1<Either.T, S>, C>) -> Either<S, D> {
    return { a1, a2, a3 ->
      val i1 = Either.narrow(a1)
      val i2 = Either.narrow(a2)
      val i3 = Either.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
} 
