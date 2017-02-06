package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface ReaderFunctor<R> : Functor<K1<Reader.T, R>> {
    override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Reader.T, R>, A>): Reader<R, B> {
        val reader = Reader.narrow(v)
        return Reader { r -> f(reader.run(r)) }
    }
}
