package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.representable.Representable

/** Representable instance for [Reader], represented by its environment type `R`. */
interface ReaderRepresentable<R> : Representable<K1<Reader.T, R>, R>, ReaderFunctor<R> {
    override fun <A> tabulate(f: (R) -> A): Reader<R, A> = Reader(f)
    override fun <A> index(fa: K1<K1<Reader.T, R>, A>): (R) -> A = Reader.narrow(fa).run
}
