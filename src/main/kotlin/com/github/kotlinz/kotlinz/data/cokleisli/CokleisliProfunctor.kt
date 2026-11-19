package com.github.kotlinz.kotlinz.data.cokleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.Comonad
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Cokleisli]: maps over the input context and the output. */
interface CokleisliProfunctor<W> : Profunctor<Cokleisli.T, W> {
    /** The comonad whose functor maps the input context. */
    val comonad: Comonad<W>

    override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Cokleisli.T, W, B, C>): Cokleisli<W, A, D> {
        val c = Cokleisli.narrow(v)
        return Cokleisli(comonad) { wa -> g(c.run(comonad.fmap(f, wa))) }
    }

    override fun <A, B, C> lmap(f: (A) -> B, v: K3<Cokleisli.T, W, B, C>): Cokleisli<W, A, C> = Cokleisli.narrow(super.lmap(f, v))
    override fun <A, B, C> rmap(g: (B) -> C, v: K3<Cokleisli.T, W, A, B>): Cokleisli<W, A, C> = Cokleisli.narrow(super.rmap(g, v))
}
