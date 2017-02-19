package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.Monad

class Kleisli<F, A, B>(val monad: Monad<F>, val run: (A) -> K1<F, B>) : K3<Kleisli.T, F, A, B> {
    class T

    companion object {
        fun <F, A, B> narrow(v: K1<K2<T, F, A>, B>) : Kleisli<F, A, B> = v as Kleisli<F, A, B>

        fun <F, A> id(monad: Monad<F>): Kleisli<F, A, A> = arrow(monad).id()
        fun <F, A, B> arr(monad: Monad<F>, f: (A) -> B): Kleisli<F, A, B> = arrow(monad).arr(f)
        fun <F, A, B> swap(monad: Monad<F>): K3<T, F, Pair<A, B>, Pair<B, A>> = arrow(monad).swap()

        private fun <F> arrow(monad: Monad<F>) = object: KleisliArrow<F> { override val monad = monad }
    }

    fun <C> compose(g: K3<Kleisli.T, F, B, C>): Kleisli<F, A, C> = arrow().compose(g, this)
    fun <C> first(): Kleisli<F, Pair<A, C>, Pair<B, C>> = arrow().first(this)
    fun <C> second(): K3<T, F, Pair<C, A>, Pair<C, B>> = arrow().second(this)
    fun <C, D> split(g: K3<T, F, C, D>): K3<T, F, Pair<A,  C>, Pair<B, D>> = arrow().split(this, g)
    fun <C>combine(g: K3<T, F, A, C>): K3<T, F, A, Pair<B, C>> = arrow().combine(this, g)

    private fun arrow() = object: KleisliArrow<F> { override val monad = this@Kleisli.monad }
}
