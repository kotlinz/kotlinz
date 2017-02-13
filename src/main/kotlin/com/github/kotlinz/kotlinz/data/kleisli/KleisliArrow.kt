package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.Arrow

interface KleisliArrow<F> : Arrow<Kleisli.T, F>, KleisliCategory<F> {
    override fun <A, B> arr(f: (A) -> B): Kleisli<F, A, B> {
        return Kleisli(monad) { a -> monad.pure(f(a)) }
    }

    override fun <A, B, C> first(f: K3<Kleisli.T, F, A, B>): Kleisli<F, Pair<A, C>, Pair<B, C>> {
        return Kleisli(monad) { pair ->
            val (a, c) = pair
            val fk = Kleisli.narrow(f)
            monad.fmap({ b -> Pair(b, c) }, fk.run(a))
        }
    }
}