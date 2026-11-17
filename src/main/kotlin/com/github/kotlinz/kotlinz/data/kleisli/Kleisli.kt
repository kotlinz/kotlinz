package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.Monad

class Kleisli<F, A, B>(val monad: Monad<F>, val run: (A) -> K1<F, B>) : K3<Kleisli.T, F, A, B> {
    class T

    companion object {
        fun <F, A, B> narrow(v: K1<K2<T, F, A>, B>) : Kleisli<F, A, B> = v as Kleisli<F, A, B>

        fun <F, A> id(monad: Monad<F>): Kleisli<F, A, A> = arrow(monad).id()
        fun <F, A, B> arr(monad: Monad<F>, f: (A) -> B): Kleisli<F, A, B> = arrow(monad).arr(f)
        fun <F, A, B> swap(monad: Monad<F>): K3<T, F, Pair<A, B>, Pair<B, A>> = arrow(monad).swap()

        fun <F, A, B> app(monad: Monad<F>): Kleisli<F, Pair<K3<T, F, A, B>, A>, B> = arrowApply(monad).app()

        private fun <F> arrowApply(monad: Monad<F>) = object: KleisliArrowApply<F> { override val monad = monad }
        private fun <F> arrow(monad: Monad<F>) = object: KleisliArrow<F> { override val monad = monad }
    }

    fun <C> compose(g: K3<Kleisli.T, F, B, C>): Kleisli<F, A, C> = arrow().compose(g, this)
    fun <C> first(): Kleisli<F, Pair<A, C>, Pair<B, C>> = arrow().first(this)
    fun <C> second(): K3<T, F, Pair<C, A>, Pair<C, B>> = arrow().second(this)
    fun <C, D> split(g: K3<T, F, C, D>): K3<T, F, Pair<A,  C>, Pair<B, D>> = arrow().split(this, g)
    fun <C>combine(g: K3<T, F, A, C>): K3<T, F, A, Pair<B, C>> = arrow().combine(this, g)

    private fun arrow() = object: KleisliArrow<F> { override val monad = this@Kleisli.monad }

    fun <Z, C> dimap(f: (Z) -> A, g: (B) -> C): Kleisli<F, Z, C> = profunctor().dimap(f, g, this)
    infix fun <Z> lmap(f: (Z) -> A): Kleisli<F, Z, B> = profunctor().lmap(f, this)
    infix fun <C> rmap(g: (B) -> C): Kleisli<F, A, C> = profunctor().rmap(g, this)

    fun <C> left(): Kleisli<F, Either<A, C>, Either<B, C>> = choice().left(this)
    fun <C> right(): Kleisli<F, Either<C, A>, Either<C, B>> = choice().right(this)

    fun <C, D> splitChoice(g: K3<T, F, C, D>): Kleisli<F, Either<A, C>, Either<B, D>> = arrowChoice().splitChoice(this, g)
    fun <C> fanin(g: K3<T, F, C, B>): Kleisli<F, Either<A, C>, B> = arrowChoice().fanin(this, g)

    private fun profunctor() = object: KleisliProfunctor<F> { override val monad = this@Kleisli.monad }
    private fun choice() = object: KleisliChoice<F> { override val monad = this@Kleisli.monad }
    private fun arrowChoice() = object: KleisliArrowChoice<F> { override val monad = this@Kleisli.monad }
}
