package com.github.kotlinz.kotlinz.data.cokleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Compose
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Compose instance for [Cokleisli]: `g` is extended over the context before `f` runs. */
interface CokleisliCompose<W> : Compose<Cokleisli.T, W> {
    /** The comonad used to extend contexts. */
    val comonad: Comonad<W>

    override fun <A, B, C> compose(f: K3<Cokleisli.T, W, B, C>, g: K3<Cokleisli.T, W, A, B>): Cokleisli<W, A, C> {
        val fc = Cokleisli.narrow(f)
        val gc = Cokleisli.narrow(g)
        return Cokleisli(comonad) { wa -> fc.run(comonad.extend(gc.run, wa)) }
    }
}
