package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.group.Monoid

class RWS<R, W: Monoid<*, W>, S, A>(val type: Class<W>, val run: (R, S) -> Triple<A, S, W>): K4<RWS.T, R, W, S, A> {
  class T

  companion object {
    fun <R, W: Monoid<*, W>, S, A> narrow(v: K1<K3<T, R, W, S>, A>) = v as RWS<R, W, S, A>

    // Monad
    fun <R, W: Monoid<*, W>, S, A> pure(type: Class<W>, v: A): RWS<R, W, S, A> = monad<R, W, S>(type).pure(v)
    fun <R, W: Monoid<*, W>, S, A> join(type: Class<W>, v: K1<K3<T, R, W, S>, K1<K3<T, R, W, S>, A>>): RWS<R, W, S, A> = monad<R, W, S>(type).join(v)
    private fun <R, W: Monoid<*, W>, S> monad(type: Class<W>) = object: RWSMonad<R, W, S> { override val type = type }

    // ApplicativeOps
    fun <R, W: Monoid<*, W>, S, A, B> liftA(type: Class<W>, f: (A) -> B) = applicativeOps<R, W, S>(type).liftA(f)
    fun <R, W: Monoid<*, W>, S, A, B, C> liftA2(type: Class<W>, f: (A, B) -> C) = applicativeOps<R, W, S>(type).liftA2(f)
    fun <R, W: Monoid<*, W>, S, A, B, C, D> liftA3(type: Class<W>, f: (A, B, C) -> D) = applicativeOps<R, W, S>(type).liftA3(f)
    private fun <R, W: Monoid<*, W>, S> applicativeOps(type: Class<W>) = object: RWSApplicativeOps<R, W, S> { override val type = type }

    // MonadOps
    fun <R, W: Monoid<*, W>, S, A, B> liftM(type: Class<W>, f: (A) -> B) = monadOps<R, W, S>(type).liftM(f)
    fun <R, W: Monoid<*, W>, S, A, B, C> liftM2(type: Class<W>, f: (A, B) -> C) = monadOps<R, W, S>(type).liftM2(f)
    fun <R, W: Monoid<*, W>, S, A, B, C, D> liftM3(type: Class<W>, f: (A, B, C) -> D) = monadOps<R, W, S>(type).liftM3(f)
    private fun <R, W: Monoid<*, W>, S> monadOps(type: Class<W>) = object: RWSMonadOps<R, W, S> { override val type = type }

    // Primitives
    fun <R, W: Monoid<*, W>, S> ask(type: Class<W>): RWS<R, W, S, R> = RWS(type) { r, s -> Triple(r, s, Monoid.mzero(type)) }
    fun <R, W: Monoid<*, W>, S> tell(type: Class<W>, w: W): RWS<R, W, S, Unit> = RWS(type) { r, s -> Triple(Unit, s, w) }
    fun <R, W: Monoid<*, W>, S> get(type: Class<W>): RWS<R, W, S, S> = RWS(type) { r, s -> Triple(s, s, Monoid.mzero(type)) }
    fun <R, W: Monoid<*, W>, S> put(type: Class<W>, s: S): RWS<R, W, S, Unit> = RWS(type) { r, _s -> Triple(Unit, s, Monoid.mzero(type)) }
    fun <R, W: Monoid<*, W>, S> modify(type: Class<W>, f: (S) -> S): RWS<R, W, S, Unit> = RWS(type) { r, s -> Triple(Unit, f(s), Monoid.mzero(type)) }
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): RWS<R, W, S, B> = monad.fmap(f, this)
  infix fun <B> ap(f: RWS<R, W, S, (A) -> B>): RWS<R, W, S, B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> RWS<R, W, S, B>): RWS<R, W, S, B> = monad.bind(f, this)
  private val monad = object: RWSMonad<R, W, S> { override val type = this@RWS.type }

  fun local(f: (R) -> R): RWS<R, W, S, A> = RWS(type) { r, s -> run(f(r), s) }
  fun eval(r: R, s: S): Pair<A, W> = run(r, s).let { Pair(it.first, it.third) }
  fun exec(r: R, s: S): Pair<S, W> = run(r, s).let { Pair(it.second, it.third) }
}
