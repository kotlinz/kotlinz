package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [IorT]; requires the underlying [monad] and the Semigroup class [type] of the left side. */
interface IorTFunctor<F, A: Semigroup<*, A>> : Functor<K2<IorT.T, F, A>> {
    /** The [Monad] instance for the underlying effect. */
    val monad: Monad<F>
    /** The Semigroup class used to combine left values. */
    val type: Class<A>

    override fun <B, C> fmap(f: (B) -> C, v: K1<K2<IorT.T, F, A>, B>): IorT<F, A, C> {
        return IorT(monad, type, monad.fmap({ ior: Ior<A, B> -> ior fmap f }, IorT.narrow(v).run))
    }
}
