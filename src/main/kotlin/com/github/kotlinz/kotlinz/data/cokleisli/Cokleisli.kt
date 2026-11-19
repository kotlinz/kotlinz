package com.github.kotlinz.kotlinz.data.cokleisli

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.Comonad

/**
 * A co-monadic function `(W<A>) -> B` treated as an arrow, for some comonad [W].
 *
 * The dual of [com.github.kotlinz.kotlinz.data.kleisli.Kleisli]: each arrow consumes a whole
 * context, and composition uses `extend` so that later arrows see the context of earlier results.
 *
 * Instances: Compose, Category, Arrow, Profunctor (each requires the underlying `Comonad<W>`).
 *
 * ```kotlin
 * val comonad = object : NonEmptyListComonad {}
 * val total = Cokleisli(comonad) { w: K1<NonEmptyList.T, Int> -> NonEmptyList.narrow(w).let { it.head + it.tail.sum() } }
 * val size = Cokleisli(comonad) { w: K1<NonEmptyList.T, Int> -> NonEmptyList.narrow(w).tail.size + 1 }
 * total.run(nonEmptyListOf(1, 2, 3))            // 6
 * total.andThen(size).run(nonEmptyListOf(1, 2)) // 2
 * ```
 *
 * @property comonad the comonad providing `extract` and `extend`.
 * @property run the underlying function.
 */
class Cokleisli<W, A, B>(val comonad: Comonad<W>, val run: (K1<W, A>) -> B) : K3<Cokleisli.T, W, A, B> {
    /** Witness type used to encode `Cokleisli` as a higher-kinded `K3<Cokleisli.T, W, A, B>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [Cokleisli]. */
        fun <W, A, B> narrow(v: K1<K2<T, W, A>, B>): Cokleisli<W, A, B> = v as Cokleisli<W, A, B>

        /** The identity arrow, returning the focused value via `extract`. */
        fun <W, A> id(comonad: Comonad<W>): Cokleisli<W, A, A> = arrow(comonad).id()
        /** Lifts a plain function into a `Cokleisli` arrow acting on the focused value. */
        fun <W, A, B> arr(comonad: Comonad<W>, f: (A) -> B): Cokleisli<W, A, B> = arrow(comonad).arr(f)

        private fun <W> arrow(comonad: Comonad<W>) = object : CokleisliArrow<W> { override val comonad = comonad }
    }

    /** Applies the arrow to [wa]. */
    operator fun invoke(wa: K1<W, A>): B = run(wa)

    /** Runs [g] first, then this arrow. */
    infix fun <C> compose(g: K3<T, W, C, A>): Cokleisli<W, C, B> = arrow().compose(this, g)
    /** Runs this arrow first, then [g]. */
    infix fun <C> andThen(g: K3<T, W, B, C>): Cokleisli<W, A, C> = arrow().compose(g, this)
    /** Applies this arrow to the first component of a pair, passing the focused second component through. */
    fun <C> first(): Cokleisli<W, Pair<A, C>, Pair<B, C>> = arrow().first(this)
    /** Applies this arrow to the second component of a pair, passing the focused first component through. */
    fun <C> second(): Cokleisli<W, Pair<C, A>, Pair<C, B>> = narrow(arrow().second(this))
    /** Runs this arrow on the first components and [g] on the second components. */
    infix fun <C, D> split(g: K3<T, W, C, D>): Cokleisli<W, Pair<A, C>, Pair<B, D>> = narrow(arrow().split(this, g))
    /** Runs this arrow and [g] on the same context and pairs the results. */
    infix fun <C> combine(g: K3<T, W, A, C>): Cokleisli<W, A, Pair<B, C>> = narrow(arrow().combine(this, g))
    private fun arrow() = object : CokleisliArrow<W> { override val comonad = this@Cokleisli.comonad }

    /** Maps [f] over the context before running and post-composes [g]. */
    fun <Z, C> dimap(f: (Z) -> A, g: (B) -> C): Cokleisli<W, Z, C> = profunctor().dimap(f, g, this)
    /** Maps [f] over the context before running. */
    infix fun <Z> lmap(f: (Z) -> A): Cokleisli<W, Z, B> = profunctor().lmap(f, this)
    /** Post-composes [g] onto the result. */
    infix fun <C> rmap(g: (B) -> C): Cokleisli<W, A, C> = profunctor().rmap(g, this)
    private fun profunctor() = object : CokleisliProfunctor<W> { override val comonad = this@Cokleisli.comonad }
}
