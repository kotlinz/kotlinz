package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.ArrowLoop
import com.github.kotlinz.kotlinz.type.monad.MonadFix

/** ArrowLoop instance for [Kleisli] over a [MonadFix]: the feedback is the `mfix` of the arrow's output. */
interface KleisliArrowLoop<F> : ArrowLoop<Kleisli.T, F>, KleisliArrow<F> {
    override val monad: MonadFix<F>

    override fun <A, B, D> loop(f: K3<Kleisli.T, F, Pair<A, () -> D>, Pair<B, D>>): Kleisli<F, A, B> {
        val fk = Kleisli.narrow(f)
        return Kleisli(monad) { a ->
            monad.fmap({ p -> p.first }, monad.mfix { p: () -> Pair<B, D> -> fk.run(Pair(a, { p().second })) })
        }
    }
}
