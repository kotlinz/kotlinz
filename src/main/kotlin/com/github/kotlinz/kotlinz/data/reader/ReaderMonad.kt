package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

interface ReaderMonad<R> : Monad<K1<Reader.T, R>>, ReaderApplicative<R>, ReaderFunctor<R> {
    override fun <A> join(v: K1<K1<Reader.T, R>, K1<K1<Reader.T, R>, A>>): Reader<R, A> {
        val reader = Reader.narrow(v)
        return Reader { r -> Reader.narrow(reader.run(r)).run(r) }
    }

    override fun <A, B> bind(f: (A) -> K1<K1<Reader.T, R>, B>, v: K1<K1<Reader.T, R>, A>): Reader<R, B> {
        return Reader.narrow(super.bind(f, v))
    }
}