package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * A monadic function `(A) -> F<B>` treated as an arrow, for some monad [F].
 *
 * Use it to compose effectful functions as first-class values: chain them with [compose], adapt
 * inputs/outputs with [dimap], and route through pairs or [Either] with the arrow combinators.
 *
 * Instances: Profunctor, Strong, Choice, Compose, Category, Arrow, ArrowChoice, ArrowApply
 * (each requires the underlying `Monad<F>`).
 *
 * ```kotlin
 * val monad = object : IdentityMonad {}
 * val inc = Kleisli(monad) { a: Int -> Identity(a + 1) }
 * val double = Kleisli(monad) { a: Int -> Identity(a * 2) }
 * inc.compose(double).run(3)                             // Identity(8)
 * inc.first<String>().run(Pair(1, "x"))                  // Identity((2, x))
 * inc.dimap({ s: String -> s.length }, { "n=$it" }).run("ab") // Identity(n=3)
 * ```
 *
 * @property monad the monad used to sequence effects.
 * @property run the underlying function.
 */
class Kleisli<F, A, B>(val monad: Monad<F>, val run: (A) -> K1<F, B>) : K3<Kleisli.T, F, A, B> {
    /** Witness type used to encode `Kleisli` as a higher-kinded `K3<Kleisli.T, F, A, B>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [Kleisli]. */
        fun <F, A, B> narrow(v: K1<K2<T, F, A>, B>) : Kleisli<F, A, B> = v as Kleisli<F, A, B>

        /** The identity arrow, returning its input via `pure`. */
        fun <F, A> id(monad: Monad<F>): Kleisli<F, A, A> = arrow(monad).id()
        /** Lifts a pure function into a `Kleisli` arrow. */
        fun <F, A, B> arr(monad: Monad<F>, f: (A) -> B): Kleisli<F, A, B> = arrow(monad).arr(f)
        /** An arrow that swaps the components of a pair. */
        fun <F, A, B> swap(monad: Monad<F>): K3<T, F, Pair<A, B>, Pair<B, A>> = arrow(monad).swap()

        /** An arrow that applies the arrow in the first component to the value in the second. */
        fun <F, A, B> app(monad: Monad<F>): Kleisli<F, Pair<K3<T, F, A, B>, A>, B> = arrowApply(monad).app()

        private fun <F> arrowApply(monad: Monad<F>) = object: KleisliArrowApply<F> { override val monad = monad }
        private fun <F> arrow(monad: Monad<F>) = object: KleisliArrow<F> { override val monad = monad }
    }

    /** Runs this arrow, then feeds its result into [g] (left-to-right composition). */
    fun <C> compose(g: K3<Kleisli.T, F, B, C>): Kleisli<F, A, C> = arrow().compose(g, this)
    /** Applies this arrow to the first component of a pair, passing the second through. */
    fun <C> first(): Kleisli<F, Pair<A, C>, Pair<B, C>> = arrow().first(this)
    /** Applies this arrow to the second component of a pair, passing the first through. */
    fun <C> second(): K3<T, F, Pair<C, A>, Pair<C, B>> = arrow().second(this)
    /** Runs this arrow on the first component and [g] on the second. */
    fun <C, D> split(g: K3<T, F, C, D>): K3<T, F, Pair<A,  C>, Pair<B, D>> = arrow().split(this, g)
    /** Runs this arrow and [g] on the same input and pairs the results. */
    fun <C>combine(g: K3<T, F, A, C>): K3<T, F, A, Pair<B, C>> = arrow().combine(this, g)

    private fun arrow() = object: KleisliArrow<F> { override val monad = this@Kleisli.monad }

    /** Pre-composes [f] on the input and post-maps [g] over the output. */
    fun <Z, C> dimap(f: (Z) -> A, g: (B) -> C): Kleisli<F, Z, C> = profunctor().dimap(f, g, this)
    /** Pre-composes [f] on the input. */
    infix fun <Z> lmap(f: (Z) -> A): Kleisli<F, Z, B> = profunctor().lmap(f, this)
    /** Maps [g] over the output. */
    infix fun <C> rmap(g: (B) -> C): Kleisli<F, A, C> = profunctor().rmap(g, this)

    /** Applies this arrow to [Either.Left] inputs, passing [Either.Right] through. */
    fun <C> left(): Kleisli<F, Either<A, C>, Either<B, C>> = choice().left(this)
    /** Applies this arrow to [Either.Right] inputs, passing [Either.Left] through. */
    fun <C> right(): Kleisli<F, Either<C, A>, Either<C, B>> = choice().right(this)

    /** Runs this arrow on [Either.Left] inputs and [g] on [Either.Right] inputs. */
    fun <C, D> splitChoice(g: K3<T, F, C, D>): Kleisli<F, Either<A, C>, Either<B, D>> = arrowChoice().splitChoice(this, g)
    /** Merges two arrows with the same output: this one for [Either.Left], [g] for [Either.Right]. */
    fun <C> fanin(g: K3<T, F, C, B>): Kleisli<F, Either<A, C>, B> = arrowChoice().fanin(this, g)

    private fun profunctor() = object: KleisliProfunctor<F> { override val monad = this@Kleisli.monad }
    private fun choice() = object: KleisliChoice<F> { override val monad = this@Kleisli.monad }
    private fun arrowChoice() = object: KleisliArrowChoice<F> { override val monad = this@Kleisli.monad }
}
