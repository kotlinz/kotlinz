package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface ReaderApplicative<R> : Applicative<K1<Reader.T, R>> {
    override fun <A> pure(v: A): Reader<R, A> = Reader { r -> v }

    override fun <A, B> ap(f: K1<K1<Reader.T, R>, (A) -> B>, v: K1<K1<Reader.T, R>, A>): Reader<R, B> {
        val reader = Reader.narrow(v)
        val readerf = Reader.narrow(f)
        return Reader { r -> Reader.narrow(fmap(readerf.run(r), reader)).run(r) }
    }
}