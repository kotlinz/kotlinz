package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad

class StateT<F, S, A>(val monad: Monad<F>, val run: (S) -> K1<F, Pair<A, S>>) : K3<StateT.T, F, S, A> {
    class T

    companion object {
        fun <F, S, A> narrow(v: K1<K2<T, F, S>, A>): StateT<F, S, A> = v as StateT<F, S, A>

        // Monad
        fun <F, S, A> pure(monad: Monad<F>, v: A): StateT<F, S, A> = instance<F, S>(monad).pure(v)
        fun <F, S, A> join(monad: Monad<F>, v: K1<K2<T, F, S>, K1<K2<T, F, S>, A>>): StateT<F, S, A> = instance<F, S>(monad).join(v)
        fun <F, S, A> fx(monad: Monad<F>, block: suspend MonadScope<K2<T, F, S>>.() -> A): StateT<F, S, A> = narrow(instance<F, S>(monad).fx(block))

        // MonadState
        fun <F, S> get(monad: Monad<F>): StateT<F, S, S> = instance<F, S>(monad).get()
        fun <F, S> put(monad: Monad<F>, s: S): StateT<F, S, Unit> = instance<F, S>(monad).put(s)
        fun <F, S> modify(monad: Monad<F>, f: (S) -> S): StateT<F, S, Unit> = instance<F, S>(monad).modify(f)
        fun <F, S, A> gets(monad: Monad<F>, f: (S) -> A): StateT<F, S, A> = narrow(instance<F, S>(monad).gets(f))
        fun <F, S, A> state(monad: Monad<F>, f: (S) -> Pair<A, S>): StateT<F, S, A> = instance<F, S>(monad).state(f)

        // Transformer
        fun <F, S, A> lift(monad: Monad<F>, v: K1<F, A>): StateT<F, S, A> = StateT(monad) { s -> monad.fmap({ a -> Pair(a, s) }, v) }

        private fun <F, S> instance(monad: Monad<F>) = object: StateTMonadState<F, S> { override val monad = monad }
    }

    // Monad
    infix fun <B> fmap(f: (A) -> B): StateT<F, S, B> = instance().fmap(f, this)
    infix fun <B> ap(f: StateT<F, S, (A) -> B>): StateT<F, S, B> = instance().ap(f, this)
    infix fun <B> bind(f: (A) -> StateT<F, S, B>): StateT<F, S, B> = instance().bind(f, this)

    fun eval(s: S): K1<F, A> = monad.fmap({ pair: Pair<A, S> -> pair.first }, run(s))
    fun exec(s: S): K1<F, S> = monad.fmap({ pair: Pair<A, S> -> pair.second }, run(s))

    private fun instance() = object: StateTMonadState<F, S> { override val monad = this@StateT.monad }
}
