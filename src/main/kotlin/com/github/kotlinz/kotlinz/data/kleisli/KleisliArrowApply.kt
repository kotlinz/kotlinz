package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.ArrowApply

interface KleisliArrowApply<F> : ArrowApply<Kleisli.T, F>, KleisliArrow<F> {
    override fun <A, B> app(): Kleisli<F, Pair<K3<Kleisli.T, F, A, B>, A>, B> =
            Kleisli(monad) { (k, a) -> Kleisli.narrow(k).run(a) }
}
