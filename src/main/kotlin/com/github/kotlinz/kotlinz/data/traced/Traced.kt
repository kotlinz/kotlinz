package com.github.kotlinz.kotlinz.data.traced

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid

class Traced<M: Monoid<*, M>, A>(val type: Class<M>, val run: (M) -> A): K2<Traced.T, M, A> {
  class T

  companion object {
    fun <M: Monoid<*, M>, A> narrow(v: K1<K1<T, M>, A>): Traced<M, A> = v as Traced<M, A>
  }

  fun trace(m: M): A = run(m)
  fun traces(f: (A) -> M): A = run(f(extract()))
  fun listen(): Traced<M, Pair<A, M>> = Traced(type) { m -> Pair(run(m), m) }
  fun censor(f: (M) -> M): Traced<M, A> = Traced(type) { m -> run(f(m)) }

  // Comonad
  infix fun <B> fmap(f: (A) -> B): Traced<M, B> = comonad.fmap(f, this)
  fun extract(): A = comonad.extract(this)
  fun duplicate(): Traced<M, K1<K1<T, M>, A>> = comonad.duplicate(this)
  infix fun <B> extend(f: (K1<K1<T, M>, A>) -> B): Traced<M, B> = comonad.extend(f, this)
  private val comonad = object: TracedComonad<M> { override val type = this@Traced.type }
}
