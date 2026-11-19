package com.github.kotlinz.kotlinz.data.selectt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [SelectT]; requires the inner [Monad] via [monad]. */
interface SelectTFunctor<R, F> : Functor<K2<SelectT.T, R, F>> {
    /** The inner monad instance. */
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<SelectT.T, R, F>, A>): SelectT<R, F, B> {
        val select = SelectT.narrow(v)
        return SelectT(monad) { k -> monad.fmap(f, select.run { a -> k(f(a)) }) }
    }
}
