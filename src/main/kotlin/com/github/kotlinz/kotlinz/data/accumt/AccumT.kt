package com.github.kotlinz.kotlinz.data.accumt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadIO
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * Accumulation monad transformer: append-only state of type `W` (a [Monoid] whose class is [type]) over a monad `F`.
 *
 * [run] receives everything accumulated so far and returns the value plus only the newly [add]ed output.
 * Unlike a writer, the accumulated output can be read back with [look]; unlike a state, it can only grow.
 * Instances: [AccumTFunctor], [AccumTApplicative], [AccumTMonad], [AccumTMonadTrans], [AccumTMonadIO] and [AccumTMFunctor].
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val type = StringMonoid::class.java
 * val program = AccumT.add<Identity.T, StringMonoid>(identity, type, StringMonoid("a")) bind {
 *     AccumT.look<Identity.T, StringMonoid>(identity, type)
 * }
 * program.run(StringMonoid(">")) // Identity(Pair(StringMonoid(">a"), StringMonoid("a")))
 * ```
 *
 * @property monad the underlying monad instance.
 * @property type the Monoid class of the accumulated output.
 * @property run the wrapped function from accumulated output to value and new output.
 */
class AccumT<F, W: Monoid<*, W>, A>(val monad: Monad<F>, val type: Class<W>, val run: (W) -> K1<F, Pair<A, W>>) : K3<AccumT.T, F, W, A> {
    /** Higher-kinded witness tag for [AccumT]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [AccumT]. */
        fun <F, W: Monoid<*, W>, A> narrow(v: K1<K2<T, F, W>, A>): AccumT<F, W, A> = v as AccumT<F, W, A>

        // Monad
        /** Returns [v] without adding output. */
        fun <F, W: Monoid<*, W>, A> pure(monad: Monad<F>, type: Class<W>, v: A): AccumT<F, W, A> = instance(monad, type).pure(v)
        /** Flattens a nested [AccumT]. */
        fun <F, W: Monoid<*, W>, A> join(monad: Monad<F>, type: Class<W>, v: K1<K2<T, F, W>, K1<K2<T, F, W>, A>>): AccumT<F, W, A> = instance(monad, type).join(v)
        /** Runs a monad comprehension block in [AccumT]. */
        fun <F, W: Monoid<*, W>, A> fx(monad: Monad<F>, type: Class<W>, block: suspend MonadScope<K2<T, F, W>>.() -> A): AccumT<F, W, A> = narrow(instance(monad, type).fx(block))

        // Primitives
        /** Returns everything accumulated so far. */
        fun <F, W: Monoid<*, W>> look(monad: Monad<F>, type: Class<W>): AccumT<F, W, W> = looks(monad, type) { it }
        /** Returns a projection of everything accumulated so far. */
        fun <F, W: Monoid<*, W>, A> looks(monad: Monad<F>, type: Class<W>, f: (W) -> A): AccumT<F, W, A> = accum(monad, type) { w -> Pair(f(w), Monoid.mzero(type)) }
        /** Appends [w] to the accumulated output. */
        fun <F, W: Monoid<*, W>> add(monad: Monad<F>, type: Class<W>, w: W): AccumT<F, W, Unit> = accum(monad, type) { Pair(Unit, w) }
        /** Embeds a pure function from the accumulated output to a value and new output. */
        fun <F, W: Monoid<*, W>, A> accum(monad: Monad<F>, type: Class<W>, f: (W) -> Pair<A, W>): AccumT<F, W, A> = AccumT(monad, type) { w -> monad.pure(f(w)) }

        // Transformer
        /** Lifts an inner computation without adding output. */
        fun <F, W: Monoid<*, W>, A> lift(monad: Monad<F>, type: Class<W>, v: K1<F, A>): AccumT<F, W, A> = instance(monad, type).lift(v)
        /** Lifts an [IO] computation through the base monad's MonadIO instance. */
        fun <F, W: Monoid<*, W>, A> liftIO(monad: MonadIO<F>, type: Class<W>, v: IO<A>): AccumT<F, W, A> {
            return object: AccumTMonadIO<F, W> {
                override val monad = monad
                override val type = type
            }.liftIO(v)
        }

        private fun <F, W: Monoid<*, W>> instance(monad: Monad<F>, type: Class<W>) = object: AccumTMonadTrans<F, W> {
            override val monad = monad
            override val type = type
        }
    }

    // Monad
    /** Maps the value. */
    infix fun <B> fmap(f: (A) -> B): AccumT<F, W, B> = instance().fmap(f, this)
    /** Applies the function produced by [f]; the value side sees the output added by [f]. */
    infix fun <B> ap(f: AccumT<F, W, (A) -> B>): AccumT<F, W, B> = instance().ap(f, this)
    /** Sequences [f]; it sees the output added by this computation. */
    infix fun <B> bind(f: (A) -> AccumT<F, W, B>): AccumT<F, W, B> = instance().bind(f, this)

    // MFunctor
    /** Swaps the base monad using [nt]; [target] is the monad of the new base. */
    fun <G> hoist(target: Monad<G>, nt: NaturalTransformation<F, G>): AccumT<G, W, A> {
        val type = this.type
        return object: AccumTMFunctor<F, G, W> {
            override val target = target
            override val type = type
        }.hoist(nt, this)
    }

    /** Runs from accumulated output [w] and returns only the value. */
    fun eval(w: W): K1<F, A> = monad.fmap({ pair: Pair<A, W> -> pair.first }, run(w))
    /** Runs from accumulated output [w] and returns only the newly added output. */
    fun exec(w: W): K1<F, W> = monad.fmap({ pair: Pair<A, W> -> pair.second }, run(w))

    private fun instance() = instance(monad, type)
}
