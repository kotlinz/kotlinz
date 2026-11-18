package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.MonadRec

/** Stack-safe tail-recursive monad instance for [Identity]. */
interface IdentityMonadRec: MonadRec<Identity.T>, IdentityMonad {
  override fun <A, B> tailRecM(a: A, f: (A) -> K1<Identity.T, Either<A, B>>): Identity<B> {
    val step = { x: A -> Identity.narrow(f(x)).value }
    val last = generateSequence(step(a)) { e -> (e as? Either.Left)?.let { step(it.value) } }.last()
    return Identity((last as Either.Right).value)
  }
}
