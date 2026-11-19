package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [IorT]; accumulates left values and short-circuits on a pure left. */
interface IorTMonad<F, A: Semigroup<*, A>> : Monad<K2<IorT.T, F, A>>, IorTApplicative<F, A>, IorTFunctor<F, A> {
    override fun <B> join(v: K1<K2<IorT.T, F, A>, K1<K2<IorT.T, F, A>, B>>): IorT<F, A, B> = bind({ it }, v)

    override fun <B, C> bind(f: (B) -> K1<K2<IorT.T, F, A>, C>, v: K1<K2<IorT.T, F, A>, B>): IorT<F, A, C> {
        return chain(IorT.narrow(v)) { b -> IorT.narrow(f(b)) }
    }
}
