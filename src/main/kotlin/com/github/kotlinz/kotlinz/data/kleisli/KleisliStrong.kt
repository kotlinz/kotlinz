package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/** Strong instance for [Kleisli]. */
interface KleisliStrong<F> : Strong<Kleisli.T, F>, KleisliProfunctor<F> {
    override fun <A, B, C> first(v: K3<Kleisli.T, F, A, B>): Kleisli<F, Pair<A, C>, Pair<B, C>> {
        val k = Kleisli.narrow(v)
        return Kleisli(monad) { (a, c) -> monad.fmap({ b -> Pair(b, c) }, k.run(a)) }
    }

    override fun <A, B, C> second(v: K3<Kleisli.T, F, A, B>): Kleisli<F, Pair<C, A>, Pair<C, B>> =
            Kleisli.narrow(super.second(v))
}
