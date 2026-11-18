package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.arrow.ArrowChoice

/** ArrowChoice instance for [Kleisli]. */
interface KleisliArrowChoice<F> : ArrowChoice<Kleisli.T, F>, KleisliArrow<F>, KleisliChoice<F> {
    override fun <A, B, C> left(f: K3<Kleisli.T, F, A, B>): Kleisli<F, Either<A, C>, Either<B, C>> = super<KleisliChoice>.left(f)
    override fun <A, B, C> right(f: K3<Kleisli.T, F, A, B>): Kleisli<F, Either<C, A>, Either<C, B>> = super<KleisliChoice>.right(f)

    override fun <A, B, C, D> splitChoice(f: K3<Kleisli.T, F, A, B>, g: K3<Kleisli.T, F, C, D>): Kleisli<F, Either<A, C>, Either<B, D>> =
            Kleisli.narrow(super.splitChoice(f, g))

    override fun <A, B, C> fanin(f: K3<Kleisli.T, F, A, C>, g: K3<Kleisli.T, F, B, C>): Kleisli<F, Either<A, B>, C> =
            Kleisli.narrow(super.fanin(f, g))
}
