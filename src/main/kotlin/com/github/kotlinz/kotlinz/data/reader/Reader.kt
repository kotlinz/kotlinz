package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

class Reader<R, A>(val run: (R) -> A) : K2<Reader.T, R, A> {
    class T

    companion object {
        fun <R, A> narrow(v: K1<K1<T, R>, A>) = v as Reader<R, A>
        
        // Monad
        fun <S, A> pure(v: A): Reader<S, A> = monad<S>().pure(v)
        fun <S, A> join(v: K1<K1<T, S>, K1<K1<T, S>, A>>): Reader<S, A> = monad<S>().join(v)
        private fun <S> monad() = object: ReaderMonad<S> {}

        // ApplicativeOps
        fun <S, A, B> liftA(f: (A) -> B) = applicativeOps<S>().liftA(f)
        fun <S, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<S>().liftA2(f)
        fun <S, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<S>().liftA3(f)
        private fun <S> applicativeOps() = object: ReaderApplicativeOps<S> {}

        // MonadOps
        fun <S, A, B> liftM(f: (A) -> B) = monadOps<S>().liftM(f)
        fun <S, A, B, C> liftM2(f: (A,  B) -> C) =  monadOps<S>().liftM2(f)
        fun <S, A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps<S>().liftM3(f)
        private fun <S> monadOps() = object: ReaderMonadOps<S> {}        
    }

    // Monad
    infix fun <B> fmap(f: (A) -> B): Reader<R, B> = monad.fmap(f, this)
    infix fun <B> ap(f: Reader<R, (A) -> B>): Reader<R, B> = monad.ap(f, this)
    infix fun <B> bind(f: (A) -> Reader<R, B>): Reader<R, B> = monad.bind(f, this)
    private val monad = object: ReaderMonad<R> {}

    fun local(f: (R) -> R): Reader<R, A> = Reader { r -> run(f(r)) }
}

fun <R> ask(): Reader<R, R> = Reader { it }