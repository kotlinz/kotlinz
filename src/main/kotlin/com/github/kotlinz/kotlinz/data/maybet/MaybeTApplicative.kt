package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface MaybeTApplicative<F> : Applicative<K1<MaybeT.T, F>>, MaybeTFunctor<F> {
    override fun <A> pure(v: A): MaybeT<F, A> = MaybeT(monad, monad.pure(Maybe.Just(v)))

    override fun <A, B> ap(f: K1<K1<MaybeT.T, F>, (A) -> B>, v: K1<K1<MaybeT.T, F>, A>): MaybeT<F, B> {
        val run = monad.bind({ mf: Maybe<(A) -> B> ->
            when (mf) {
                is Maybe.Just -> fmap(mf.value, v).run
                is Maybe.None -> monad.pure(Maybe.None<B>())
            }
        }, MaybeT.narrow(f).run)
        return MaybeT(monad, run)
    }
}
