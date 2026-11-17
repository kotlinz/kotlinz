package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadReader

interface ReaderMonadReader<R> : MonadReader<K1<Reader.T, R>, R>, ReaderMonad<R> {
    override fun ask(): Reader<R, R> = Reader { r -> r }

    override fun <A> local(f: (R) -> R, v: K1<K1<Reader.T, R>, A>): Reader<R, A> {
        val reader = Reader.narrow(v)
        return Reader { r -> reader.run(f(r)) }
    }

    override fun <A> reader(f: (R) -> A): Reader<R, A> = Reader(f)
}
