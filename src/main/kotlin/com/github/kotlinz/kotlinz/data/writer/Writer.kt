package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid

class Writer<W: Monoid<*, W>, A> (val type: Class<W>, val run: Pair<A, W>) : K2<Writer.T, W, A> {
    class T

    companion object {
        fun <W: Monoid<*, W>, A> narrow(v: K1<K1<T, W>, A>) = v as Writer<W, A>
        
        // Monad
        fun <W: Monoid<*, W>, A> pure(type: Class<W>, v: A): Writer<W, A> = monad(type).pure(v)
        fun <W: Monoid<*, W>, A> join(type: Class<W>, v: K1<K1<T, W>, K1<K1<T, W>, A>>): Writer<W, A> = monad(type).join(v)
        private fun <W: Monoid<*, W>> monad(type: Class<W>) = object: WriterMonad<W> { override val type = type }

        // ApplicativeOps
        fun <W: Monoid<*, W>, A, B> liftA(type: Class<W>, f: (A) -> B) = applicativeOps(type).liftA(f)
        fun <W: Monoid<*, W>, A, B, C> liftA2(type: Class<W>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
        fun <W: Monoid<*, W>, A, B, C, D> liftA3(type: Class<W>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
        private fun <W: Monoid<*, W>> applicativeOps(type: Class<W>) = object: WriterApplicativeOps<W> { override val type = type }

        // MonadOps
        fun <W: Monoid<*, W>, A, B> liftM(type: Class<W>, f: (A) -> B) = monadOps(type).liftM(f)
        fun <W: Monoid<*, W>, A, B, C> liftM2(type: Class<W>, f: (A,  B) -> C) =  monadOps(type).liftM2(f)
        fun <W: Monoid<*, W>, A, B, C, D> liftM3(type: Class<W>, f: (A, B, C) -> D) =  monadOps(type).liftM3(f)
        private fun <W: Monoid<*, W>> monadOps(type: Class<W>) = object: WriterMonadOps<W> { override val type = type }
    }

    // Monad
    infix fun <B> fmap(f: (A) -> B): Writer<W, B> = monad.fmap(f, this)
    infix fun <B> ap(f: Writer<W, (A) -> B>): Writer<W, B> = monad.ap(f, this)
    infix fun <B> bind(f: (A) -> Writer<W, B>): Writer<W, B> = monad.bind(f, this)
    private val monad = object: WriterMonad<W> { override val type = this@Writer.type }
}

