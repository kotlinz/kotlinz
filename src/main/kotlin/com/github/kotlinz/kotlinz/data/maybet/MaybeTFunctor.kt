package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [MaybeT]; requires a `Monad<F>` for the underlying effect. */
interface MaybeTFunctor<F> : Functor<K1<MaybeT.T, F>> {
    /** The underlying monad. */
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K1<MaybeT.T, F>, A>): MaybeT<F, B> {
        return MaybeT(monad, monad.fmap({ m: Maybe<A> -> m fmap f }, MaybeT.narrow(v).run))
    }
}
