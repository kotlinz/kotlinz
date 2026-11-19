package com.github.kotlinz.kotlinz.data.selectt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [SelectT]; the first choice is made by ranking each candidate's full continuation. */
interface SelectTMonad<R, F> : Monad<K2<SelectT.T, R, F>>, SelectTApplicative<R, F>, SelectTFunctor<R, F> {
    override fun <A> join(v: K1<K2<SelectT.T, R, F>, K1<K2<SelectT.T, R, F>, A>>): SelectT<R, F, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K2<SelectT.T, R, F>, B>, v: K1<K2<SelectT.T, R, F>, A>): SelectT<R, F, B> {
        val select = SelectT.narrow(v)
        return SelectT(monad) { k ->
            val h = { a: A -> SelectT.narrow(f(a)).run(k) }
            monad.bind(h, select.run { a -> monad.bind(k, h(a)) })
        }
    }
}
