package com.github.kotlinz.kotlinz.data.selectt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [SelectT]; the function is selected knowing which value it will be applied to. */
interface SelectTApplicative<R, F> : Applicative<K2<SelectT.T, R, F>>, SelectTFunctor<R, F> {
    override fun <A> pure(v: A): SelectT<R, F, A> = SelectT(monad) { monad.pure(v) }

    override fun <A, B> ap(f: K1<K2<SelectT.T, R, F>, (A) -> B>, v: K1<K2<SelectT.T, R, F>, A>): SelectT<R, F, B> {
        val (selectf, select) = Pair(SelectT.narrow(f), SelectT.narrow(v))
        return SelectT(monad) { k ->
            val h = { g: (A) -> B -> monad.fmap(g, select.run { a -> k(g(a)) }) }
            monad.bind(h, selectf.run { g -> monad.bind(k, h(g)) })
        }
    }
}
