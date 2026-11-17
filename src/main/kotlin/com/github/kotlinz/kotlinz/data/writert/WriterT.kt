package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

class WriterT<F, W: Monoid<*, W>, A>(val monad: Monad<F>, val type: Class<W>, val run: K1<F, Pair<A, W>>) : K3<WriterT.T, F, W, A> {
    class T

    companion object {
        fun <F, W: Monoid<*, W>, A> narrow(v: K1<K2<T, F, W>, A>): WriterT<F, W, A> = v as WriterT<F, W, A>

        // Monad
        fun <F, W: Monoid<*, W>, A> pure(monad: Monad<F>, type: Class<W>, v: A): WriterT<F, W, A> = instance(monad, type).pure(v)
        fun <F, W: Monoid<*, W>, A> join(monad: Monad<F>, type: Class<W>, v: K1<K2<T, F, W>, K1<K2<T, F, W>, A>>): WriterT<F, W, A> = instance(monad, type).join(v)
        fun <F, W: Monoid<*, W>, A> fx(monad: Monad<F>, type: Class<W>, block: suspend MonadScope<K2<T, F, W>>.() -> A): WriterT<F, W, A> = narrow(instance(monad, type).fx(block))

        // MonadWriter
        fun <F, W: Monoid<*, W>, A> writer(monad: Monad<F>, type: Class<W>, v: Pair<A, W>): WriterT<F, W, A> = instance(monad, type).writer(v)
        fun <F, W: Monoid<*, W>> tell(monad: Monad<F>, type: Class<W>, w: W): WriterT<F, W, Unit> = instance(monad, type).tell(w)

        // Transformer
        fun <F, W: Monoid<*, W>, A> lift(monad: Monad<F>, type: Class<W>, v: K1<F, A>): WriterT<F, W, A> {
            return WriterT(monad, type, monad.fmap({ a -> Pair(a, Monoid.mzero(type)) }, v))
        }

        private fun <F, W: Monoid<*, W>> instance(monad: Monad<F>, type: Class<W>) = object: WriterTMonadWriter<F, W> {
            override val monad = monad
            override val type = type
        }
    }

    // Monad
    infix fun <B> fmap(f: (A) -> B): WriterT<F, W, B> = instance().fmap(f, this)
    infix fun <B> ap(f: WriterT<F, W, (A) -> B>): WriterT<F, W, B> = instance().ap(f, this)
    infix fun <B> bind(f: (A) -> WriterT<F, W, B>): WriterT<F, W, B> = instance().bind(f, this)

    // MonadWriter
    fun listen(): WriterT<F, W, Pair<A, W>> = instance().listen(this)
    infix fun censor(f: (W) -> W): WriterT<F, W, A> = narrow(instance().censor(f, this))

    private fun instance() = instance(monad, type)
}
