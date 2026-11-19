package com.github.kotlinz.kotlinz.data.cokleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.Arrow

/** Arrow instance for [Cokleisli]; [first] runs the arrow on the first components and extracts the focused second. */
interface CokleisliArrow<W> : Arrow<Cokleisli.T, W>, CokleisliCategory<W> {
    override fun <A, B> arr(f: (A) -> B): Cokleisli<W, A, B> = Cokleisli(comonad) { wa -> f(comonad.extract(wa)) }

    override fun <A, B, C> first(f: K3<Cokleisli.T, W, A, B>): Cokleisli<W, Pair<A, C>, Pair<B, C>> {
        val fc = Cokleisli.narrow(f)
        return Cokleisli(comonad) { wp -> Pair(fc.run(comonad.fmap({ p: Pair<A, C> -> p.first }, wp)), comonad.extract(wp).second) }
    }
}
