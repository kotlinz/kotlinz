package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** One step of a [ListT]: `None` at the end, or the next element with the rest of the stream. */
typealias ListTStep<F, A> = Maybe<Pair<A, ListT<F, A>>>

/**
 * "ListT done right": an effectful stream whose every step is an `F<Maybe<Pair<A, ListT<F, A>>>>`.
 *
 * Unlike `F<List<A>>`, effects are interleaved with elements, so the stream is lazy, can be infinite,
 * and the monad laws hold for every base monad [F]. Steps are built on demand from a thunk, which also
 * makes infinite streams over strict monads such as `Identity` possible (see [defer]).
 *
 * Instances: Functor, Applicative, Monad, Alternative, MonadPlus, MonadTrans, MonadIO.
 *
 * ```kotlin
 * val io = object : IOMonad {}
 * val xs = ListT.fromList(io, listOf(1, 2, 3)) bind { x -> ListT.lift(io, IO { println(x); x * 10 }) }
 * IO.narrow(xs.take(2).toList()).unsafeRun() // prints 1, 2 and returns [10, 20]
 * ```
 *
 * @property monad the underlying monad instance.
 */
class ListT<F, A>(val monad: Monad<F>, private val step: () -> K1<F, ListTStep<F, A>>) : K2<ListT.T, F, A> {
    /** Witness type used to encode `ListT` as a higher-kinded `K2<ListT.T, F, A>`. */
    class T

    /** The first step of the stream; built anew on every access. */
    val run: K1<F, ListTStep<F, A>> get() = step()

    companion object {
        /** Downcasts a higher-kinded value back to [ListT]. */
        fun <F, A> narrow(v: K1<K1<T, F>, A>): ListT<F, A> = v as ListT<F, A>

        // Monad
        /** A one-element stream. */
        fun <F, A> pure(monad: Monad<F>, v: A): ListT<F, A> = instance(monad).pure(v)
        /** Flattens a stream of streams. */
        fun <F, A> join(monad: Monad<F>, v: K1<K1<T, F>, K1<K1<T, F>, A>>): ListT<F, A> = instance(monad).join(v)
        /** Builds a stream with monad comprehension syntax; the block is replayed for every element. */
        fun <F, A> fx(monad: Monad<F>, block: suspend MonadScope<K1<T, F>>.() -> A): ListT<F, A> = narrow(instance(monad).fxReplay(block))

        // Construction
        /** The empty stream. */
        fun <F, A> empty(monad: Monad<F>): ListT<F, A> = ListT(monad) { monad.pure(Maybe.None()) }
        /** Prepends [head] to [tail]. */
        fun <F, A> cons(monad: Monad<F>, head: A, tail: ListT<F, A>): ListT<F, A> = ListT(monad) { monad.pure(Maybe.Just(Pair(head, tail))) }
        /** A stream of the elements of [values]. */
        fun <F, A> fromList(monad: Monad<F>, values: List<A>): ListT<F, A> = values.foldRight(empty(monad)) { a, acc -> cons(monad, a, acc) }
        /** Builds the stream lazily on first step; needed for recursive or infinite streams. */
        fun <F, A> defer(monad: Monad<F>, f: () -> ListT<F, A>): ListT<F, A> = ListT(monad) { f().run }

        // Transformer
        /** Lifts an `F<A>` into a one-element stream. */
        fun <F, A> lift(monad: Monad<F>, v: K1<F, A>): ListT<F, A> = instance(monad).lift(v)
        /** Lifts an [IO] computation through the base monad's MonadIO instance. */
        fun <F, A> liftIO(monad: MonadIO<F>, v: IO<A>): ListT<F, A> = object : ListTMonadIO<F> { override val monad = monad }.liftIO(v)

        private fun <F> instance(monad: Monad<F>) = object : ListTMonadTrans<F>, ListTMonadPlus<F> { override val monad = monad }
    }

    // Monad
    /** Applies [f] to every element. */
    infix fun <B> fmap(f: (A) -> B): ListT<F, B> = instance().fmap(f, this)
    /** Applies every function of [f] to every element. */
    infix fun <B> ap(f: ListT<F, (A) -> B>): ListT<F, B> = instance().ap(f, this)
    /** Maps each element to a stream and concatenates the results. */
    infix fun <B> bind(f: (A) -> ListT<F, B>): ListT<F, B> = instance().bind(f, this)

    // Alternative
    /** Concatenates this stream with [other]; same as [concat]. */
    infix fun orElse(other: ListT<F, A>): ListT<F, A> = instance().orElse(this, other)

    /** Concatenates this stream with [other]. */
    infix fun concat(other: ListT<F, A>): ListT<F, A> = ListT(monad) {
        monad.bind({ s: ListTStep<F, A> ->
            when (s) {
                is Maybe.Just -> monad.pure(Maybe.Just(Pair(s.value.first, s.value.second concat other)))
                is Maybe.None -> other.run
            }
        }, run)
    }

    /** Keeps at most the first [n] elements, without running the steps after them. */
    fun take(n: Int): ListT<F, A> = if (n <= 0) empty(monad) else ListT(monad) {
        monad.fmap({ s: ListTStep<F, A> -> s fmap { (a, rest) -> Pair(a, rest.take(n - 1)) } }, run)
    }

    /** Runs the whole stream and collects its elements; diverges on infinite streams. */
    fun toList(): K1<F, List<A>> = monad.bind({ s: ListTStep<F, A> ->
        when (s) {
            is Maybe.Just -> monad.fmap({ rest: List<A> -> listOf(s.value.first) + rest }, s.value.second.toList())
            is Maybe.None -> monad.pure(emptyList())
        }
    }, run)

    private fun instance() = object : ListTMonadTrans<F>, ListTMonadPlus<F> { override val monad = this@ListT.monad }
}
