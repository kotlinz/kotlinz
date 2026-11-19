package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.K5
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadIO
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * Reader-Writer-State monad transformer: a function `(R, S) -> F<Triple<A, S, W>>` over an inner monad `F`.
 *
 * Reads an environment `R`, threads a state `S` and accumulates a log `W` (a [Monoid] whose class is [type]),
 * while running effects of `F`. Instances: [RWSTFunctor], [RWSTApplicative], [RWSTMonad], [RWSTMonadReader],
 * [RWSTMonadWriter], [RWSTMonadState], [RWSTMonadTrans], [RWSTMonadIO] and [RWSTMFunctor].
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val type = StringMonoid::class.java
 * val program = RWST.fx<Identity.T, Int, StringMonoid, Int, Int>(identity, type) {
 *     val r = RWST.ask<Identity.T, Int, StringMonoid, Int>(identity, type).bind()
 *     RWST.tell<Identity.T, Int, StringMonoid, Int>(identity, type, StringMonoid("r=$r")).bind()
 *     RWST.modify<Identity.T, Int, StringMonoid, Int>(identity, type) { it + r }.bind()
 *     r * 10
 * }
 * program.run(2, 10) // Identity(Triple(20, 12, StringMonoid("r=2")))
 * ```
 *
 * @property monad the inner monad instance.
 * @property type the Monoid class of the log.
 * @property run the wrapped function.
 */
class RWST<F, R, W: Monoid<*, W>, S, A>(
    val monad: Monad<F>,
    val type: Class<W>,
    val run: (R, S) -> K1<F, Triple<A, S, W>>
) : K5<RWST.T, F, R, W, S, A> {
    /** Higher-kinded witness tag for [RWST]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [RWST]. */
        fun <F, R, W: Monoid<*, W>, S, A> narrow(v: K1<K4<T, F, R, W, S>, A>): RWST<F, R, W, S, A> = v as RWST<F, R, W, S, A>

        // Monad
        /** Returns [v] without reading, logging or changing state. */
        fun <F, R, W: Monoid<*, W>, S, A> pure(monad: Monad<F>, type: Class<W>, v: A): RWST<F, R, W, S, A> = instance<F, R, W, S>(monad, type).pure(v)
        /** Flattens a nested [RWST]. */
        fun <F, R, W: Monoid<*, W>, S, A> join(monad: Monad<F>, type: Class<W>, v: K1<K4<T, F, R, W, S>, K1<K4<T, F, R, W, S>, A>>): RWST<F, R, W, S, A> = instance<F, R, W, S>(monad, type).join(v)
        /** Runs a monad comprehension block in [RWST]. */
        fun <F, R, W: Monoid<*, W>, S, A> fx(monad: Monad<F>, type: Class<W>, block: suspend MonadScope<K4<T, F, R, W, S>>.() -> A): RWST<F, R, W, S, A> = narrow(instance<F, R, W, S>(monad, type).fx(block))

        // MonadReader
        /** Returns the environment. */
        fun <F, R, W: Monoid<*, W>, S> ask(monad: Monad<F>, type: Class<W>): RWST<F, R, W, S, R> = instance<F, R, W, S>(monad, type).ask()
        /** Returns a projection of the environment. */
        fun <F, R, W: Monoid<*, W>, S, A> reader(monad: Monad<F>, type: Class<W>, f: (R) -> A): RWST<F, R, W, S, A> = narrow(instance<F, R, W, S>(monad, type).reader(f))

        // MonadWriter
        /** Returns the first component and logs the second. */
        fun <F, R, W: Monoid<*, W>, S, A> writer(monad: Monad<F>, type: Class<W>, v: Pair<A, W>): RWST<F, R, W, S, A> = instance<F, R, W, S>(monad, type).writer(v)
        /** Appends [w] to the log. */
        fun <F, R, W: Monoid<*, W>, S> tell(monad: Monad<F>, type: Class<W>, w: W): RWST<F, R, W, S, Unit> = instance<F, R, W, S>(monad, type).tell(w)

        // MonadState
        /** Returns the current state. */
        fun <F, R, W: Monoid<*, W>, S> get(monad: Monad<F>, type: Class<W>): RWST<F, R, W, S, S> = instance<F, R, W, S>(monad, type).get()
        /** Replaces the state with [s]. */
        fun <F, R, W: Monoid<*, W>, S> put(monad: Monad<F>, type: Class<W>, s: S): RWST<F, R, W, S, Unit> = instance<F, R, W, S>(monad, type).put(s)
        /** Updates the state with [f]. */
        fun <F, R, W: Monoid<*, W>, S> modify(monad: Monad<F>, type: Class<W>, f: (S) -> S): RWST<F, R, W, S, Unit> = instance<F, R, W, S>(monad, type).modify(f)
        /** Returns a projection of the state. */
        fun <F, R, W: Monoid<*, W>, S, A> gets(monad: Monad<F>, type: Class<W>, f: (S) -> A): RWST<F, R, W, S, A> = narrow(instance<F, R, W, S>(monad, type).gets(f))
        /** Embeds a pure state transition. */
        fun <F, R, W: Monoid<*, W>, S, A> state(monad: Monad<F>, type: Class<W>, f: (S) -> Pair<A, S>): RWST<F, R, W, S, A> = instance<F, R, W, S>(monad, type).state(f)

        // Transformer
        /** Lifts an inner computation, leaving environment, state and log untouched. */
        fun <F, R, W: Monoid<*, W>, S, A> lift(monad: Monad<F>, type: Class<W>, v: K1<F, A>): RWST<F, R, W, S, A> = instance<F, R, W, S>(monad, type).lift(v)
        /** Lifts an [IO] computation through the base monad's MonadIO instance. */
        fun <F, R, W: Monoid<*, W>, S, A> liftIO(monad: MonadIO<F>, type: Class<W>, v: IO<A>): RWST<F, R, W, S, A> {
            return object: RWSTMonadIO<F, R, W, S> {
                override val monad = monad
                override val type = type
            }.liftIO(v)
        }

        private fun <F, R, W: Monoid<*, W>, S> instance(monad: Monad<F>, type: Class<W>) =
            object: RWSTMonadReader<F, R, W, S>, RWSTMonadWriter<F, R, W, S>, RWSTMonadState<F, R, W, S>, RWSTMonadTrans<F, R, W, S> {
                override val monad = monad
                override val type = type
            }
    }

    // Monad
    /** Maps the result value. */
    infix fun <B> fmap(f: (A) -> B): RWST<F, R, W, S, B> = instance().fmap(f, this)
    /** Applies the function produced by [f], threading state and combining logs. */
    infix fun <B> ap(f: RWST<F, R, W, S, (A) -> B>): RWST<F, R, W, S, B> = instance().ap(f, this)
    /** Sequences this computation with [f], threading state and combining logs. */
    infix fun <B> bind(f: (A) -> RWST<F, R, W, S, B>): RWST<F, R, W, S, B> = instance().bind(f, this)

    // MonadReader / MonadWriter
    /** Runs this computation with an environment modified by [f]. */
    fun local(f: (R) -> R): RWST<F, R, W, S, A> = instance().local(f, this)
    /** Exposes the log alongside the value. */
    fun listen(): RWST<F, R, W, S, Pair<A, W>> = instance().listen(this)
    /** Transforms the log with [f]. */
    infix fun censor(f: (W) -> W): RWST<F, R, W, S, A> = narrow(instance().censor(f, this))

    // MFunctor
    /** Swaps the base monad using [nt]; [target] is the monad of the new base. */
    fun <G> hoist(target: Monad<G>, nt: NaturalTransformation<F, G>): RWST<G, R, W, S, A> {
        val type = this.type
        return object: RWSTMFunctor<F, G, R, W, S> {
            override val target = target
            override val type = type
        }.hoist(nt, this)
    }

    /** Runs the computation and returns the value and the log inside `F`. */
    fun eval(r: R, s: S): K1<F, Pair<A, W>> = monad.fmap({ (a, _, w): Triple<A, S, W> -> Pair(a, w) }, run(r, s))
    /** Runs the computation and returns the final state and the log inside `F`. */
    fun exec(r: R, s: S): K1<F, Pair<S, W>> = monad.fmap({ (_, s2, w): Triple<A, S, W> -> Pair(s2, w) }, run(r, s))

    private fun instance() = instance<F, R, W, S>(monad, type)
}
