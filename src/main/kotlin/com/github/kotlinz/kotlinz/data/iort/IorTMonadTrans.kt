package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [IorT]: lifted computations produce a [Ior.Right]. */
interface IorTMonadTrans<F, A: Semigroup<*, A>> : MonadTrans<F, K2<IorT.T, F, A>>, IorTMonad<F, A> {
    override fun <B> lift(v: K1<F, B>): IorT<F, A, B> = IorT(monad, type, monad.fmap({ b -> Ior.Right<A, B>(b) }, v))
}
