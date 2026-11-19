package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [MaybeT]: lifted computations always produce a present value. */
interface MaybeTMonadTrans<F> : MonadTrans<F, K1<MaybeT.T, F>>, MaybeTMonad<F> {
    override fun <A> lift(v: K1<F, A>): MaybeT<F, A> = MaybeT(monad, monad.fmap({ a -> Maybe.Just(a) }, v))
}
