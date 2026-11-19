package com.github.kotlinz.kotlinz.data.forget

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.profunctor.Cochoice

/** Cochoice instance for [Forget]: injects the input into the kept branch. */
interface ForgetCochoice<R>: Cochoice<Forget.T, R>, ForgetProfunctor<R> {
  override fun <A, B, C> unleft(v: K3<Forget.T, R, Either<A, C>, Either<B, C>>): Forget<R, A, B> {
    val forget = Forget.narrow(v)
    return Forget { a -> forget.run(Either.Left(a)) }
  }

  override fun <A, B, C> unright(v: K3<Forget.T, R, Either<C, A>, Either<C, B>>): Forget<R, A, B> {
    val forget = Forget.narrow(v)
    return Forget { a -> forget.run(Either.Right(a)) }
  }
}
