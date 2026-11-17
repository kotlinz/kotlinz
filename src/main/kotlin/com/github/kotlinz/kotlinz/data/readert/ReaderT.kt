package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad

class ReaderT<F, R, A>(val monad: Monad<F>, val run: (R) -> K1<F, A>) : K3<ReaderT.T, F, R, A> {
    class T

    companion object {
        fun <F, R, A> narrow(v: K1<K2<T, F, R>, A>): ReaderT<F, R, A> = v as ReaderT<F, R, A>

        // Monad
        fun <F, R, A> pure(monad: Monad<F>, v: A): ReaderT<F, R, A> = instance<F, R>(monad).pure(v)
        fun <F, R, A> join(monad: Monad<F>, v: K1<K2<T, F, R>, K1<K2<T, F, R>, A>>): ReaderT<F, R, A> = instance<F, R>(monad).join(v)
        fun <F, R, A> fx(monad: Monad<F>, block: suspend MonadScope<K2<T, F, R>>.() -> A): ReaderT<F, R, A> = narrow(instance<F, R>(monad).fx(block))

        // MonadReader
        fun <F, R> ask(monad: Monad<F>): ReaderT<F, R, R> = instance<F, R>(monad).ask()
        fun <F, R, A> reader(monad: Monad<F>, f: (R) -> A): ReaderT<F, R, A> = narrow(instance<F, R>(monad).reader(f))

        // Transformer
        fun <F, R, A> lift(monad: Monad<F>, v: K1<F, A>): ReaderT<F, R, A> = ReaderT(monad) { _ -> v }

        private fun <F, R> instance(monad: Monad<F>) = object: ReaderTMonadReader<F, R> { override val monad = monad }
    }

    // Monad
    infix fun <B> fmap(f: (A) -> B): ReaderT<F, R, B> = instance().fmap(f, this)
    infix fun <B> ap(f: ReaderT<F, R, (A) -> B>): ReaderT<F, R, B> = instance().ap(f, this)
    infix fun <B> bind(f: (A) -> ReaderT<F, R, B>): ReaderT<F, R, B> = instance().bind(f, this)

    // MonadReader
    fun local(f: (R) -> R): ReaderT<F, R, A> = instance().local(f, this)

    private fun instance() = object: ReaderTMonadReader<F, R> { override val monad = this@ReaderT.monad }
}
