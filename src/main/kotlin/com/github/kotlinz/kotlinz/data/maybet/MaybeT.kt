package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad

class MaybeT<F, A>(val monad: Monad<F>, val run: K1<F, Maybe<A>>) : K2<MaybeT.T, F, A> {
    class T

    companion object {
        fun <F, A> narrow(v: K1<K1<T, F>, A>): MaybeT<F, A> = v as MaybeT<F, A>

        // Monad
        fun <F, A> pure(monad: Monad<F>, v: A): MaybeT<F, A> = instance(monad).pure(v)
        fun <F, A> join(monad: Monad<F>, v: K1<K1<T, F>, K1<K1<T, F>, A>>): MaybeT<F, A> = instance(monad).join(v)
        fun <F, A> fx(monad: Monad<F>, block: suspend MonadScope<K1<T, F>>.() -> A): MaybeT<F, A> = narrow(instance(monad).fx(block))

        // Transformer
        fun <F, A> lift(monad: Monad<F>, v: K1<F, A>): MaybeT<F, A> = MaybeT(monad, monad.fmap({ a -> Maybe.Just(a) }, v))
        fun <F, A> fromMaybe(monad: Monad<F>, v: Maybe<A>): MaybeT<F, A> = MaybeT(monad, monad.pure(v))
        fun <F, A> none(monad: Monad<F>): MaybeT<F, A> = fromMaybe(monad, Maybe.None())

        private fun <F> instance(monad: Monad<F>) = object: MaybeTMonad<F> { override val monad = monad }
    }

    // Monad
    infix fun <B> fmap(f: (A) -> B): MaybeT<F, B> = instance().fmap(f, this)
    infix fun <B> ap(f: MaybeT<F, (A) -> B>): MaybeT<F, B> = instance().ap(f, this)
    infix fun <B> bind(f: (A) -> MaybeT<F, B>): MaybeT<F, B> = instance().bind(f, this)

    fun getOrElse(v: A): K1<F, A> = monad.fmap({ m: Maybe<A> -> m.getOrElse(v) }, run)

    private fun instance() = object: MaybeTMonad<F> { override val monad = this@MaybeT.monad }
}
