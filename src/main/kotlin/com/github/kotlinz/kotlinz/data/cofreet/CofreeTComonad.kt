package com.github.kotlinz.kotlinz.data.cofreet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [CofreeT]; extends the base comonad at every layer, keeping tails lazy. */
interface CofreeTComonad<F, W> : Comonad<K2<CofreeT.T, F, W>>, CofreeTFunctor<F, W> {
    override fun <A> extract(v: K1<K2<CofreeT.T, F, W>, A>): A = comonad.extract(CofreeT.narrow(v).run).head

    override fun <A> duplicate(v: K1<K2<CofreeT.T, F, W>, A>): CofreeT<F, W, K1<K2<CofreeT.T, F, W>, A>> = extend({ it }, v)

    override fun <A, B> extend(f: (K1<K2<CofreeT.T, F, W>, A>) -> B, v: K1<K2<CofreeT.T, F, W>, A>): CofreeT<F, W, B> {
        val layer = { w: K1<W, CofreeF<F, A, CofreeT<F, W, A>>> ->
            CofreeF<F, B, CofreeT<F, W, B>>(f(CofreeT(functor, comonad, w))) {
                functor.fmap({ next: CofreeT<F, W, A> -> extend(f, next) }, comonad.extract(w).tail)
            }
        }
        return CofreeT(functor, comonad, comonad.extend(layer, CofreeT.narrow(v).run))
    }
}
