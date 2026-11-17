package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.distributive.Distributive

interface ReaderDistributive<R> : Distributive<K1<Reader.T, R>>, ReaderFunctor<R> {
    override fun <F, A> distribute(functor: Functor<F>, fga: K1<F, K1<K1<Reader.T, R>, A>>): Reader<R, K1<F, A>> {
        return Reader { r -> functor.fmap({ ga: K1<K1<Reader.T, R>, A> -> Reader.narrow(ga).run(r) }, fga) }
    }
}
