package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.profunctor.Choice

/** Choice instance for [Kleisli]. */
interface KleisliChoice<F> : Choice<Kleisli.T, F>, KleisliProfunctor<F> {
    override fun <A, B, C> left(v: K3<Kleisli.T, F, A, B>): Kleisli<F, Either<A, C>, Either<B, C>> {
        val k = Kleisli.narrow(v)
        return Kleisli(monad) { e ->
            when (e) {
                is Either.Left -> monad.fmap({ b -> Either.Left<B, C>(b) }, k.run(e.value))
                is Either.Right -> monad.pure(Either.Right(e.value))
            }
        }
    }

    override fun <A, B, C> right(v: K3<Kleisli.T, F, A, B>): Kleisli<F, Either<C, A>, Either<C, B>> =
            Kleisli.narrow(super.right(v))
}
