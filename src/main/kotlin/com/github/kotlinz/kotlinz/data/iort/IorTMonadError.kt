package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [IorT]: a pure [Ior.Left] is the error; [Ior.Both] counts as a success. */
interface IorTMonadError<F, A: Semigroup<*, A>> : MonadError<K2<IorT.T, F, A>, A>, IorTMonad<F, A> {
    override fun <B> raiseError(e: A): IorT<F, A, B> = IorT(monad, type, monad.pure(Ior.Left(e)))

    override fun <B> handleErrorWith(f: (A) -> K1<K2<IorT.T, F, A>, B>, v: K1<K2<IorT.T, F, A>, B>): IorT<F, A, B> {
        val run = monad.bind({ ior: Ior<A, B> ->
            when (ior) {
                is Ior.Left -> IorT.narrow(f(ior.value)).run
                else -> monad.pure(ior)
            }
        }, IorT.narrow(v).run)
        return IorT(monad, type, run)
    }
}
