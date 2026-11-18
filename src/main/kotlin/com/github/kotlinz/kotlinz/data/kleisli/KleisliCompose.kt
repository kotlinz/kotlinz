package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Compose
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Compose instance for [Kleisli], sequencing effects with `bind`. */
interface KleisliCompose<F> : Compose<Kleisli.T, F> {
    /** The monad used to sequence effects. */
    val monad: Monad<F>

    override fun <A, B, C> compose(f: K3<Kleisli.T, F, B, C>, g: K3<Kleisli.T, F, A, B>): Kleisli<F, A, C> {
        val fk = Kleisli.narrow(f)
        val gk = Kleisli.narrow(g)
        return Kleisli(monad) { a -> monad.bind(fk.run, monad.bind(gk.run, monad.pure(a))) }
    }

}