package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [MaybeT], lifted from a base monad that has MonadError. */
interface MaybeTMonadError<F, E> : MonadError<K1<MaybeT.T, F>, E>, MaybeTMonadTrans<F> {
    /** The underlying monad with error `E`. */
    override val monad: MonadError<F, E>

    override fun <A> raiseError(e: E): MaybeT<F, A> = lift(monad.raiseError(e))

    override fun <A> handleErrorWith(f: (E) -> K1<K1<MaybeT.T, F>, A>, v: K1<K1<MaybeT.T, F>, A>): MaybeT<F, A> {
        return MaybeT(monad, monad.handleErrorWith({ e -> MaybeT.narrow(f(e)).run }, MaybeT.narrow(v).run))
    }
}
