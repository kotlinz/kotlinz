package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

interface KleisliProfunctor<F> : Profunctor<Kleisli.T, F> {
    val monad: Monad<F>

    override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Kleisli.T, F, B, C>): Kleisli<F, A, D> {
        val k = Kleisli.narrow(v)
        return Kleisli(monad) { a -> monad.fmap(g, k.run(f(a))) }
    }

    override fun <A, B, C> lmap(f: (A) -> B, v: K3<Kleisli.T, F, B, C>): Kleisli<F, A, C> = Kleisli.narrow(super.lmap(f, v))
    override fun <A, B, C> rmap(g: (B) -> C, v: K3<Kleisli.T, F, A, B>): Kleisli<F, A, C> = Kleisli.narrow(super.rmap(g, v))
}
