package com.github.kotlinz.kotlinz.data.moore

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Moore] with a fixed input type [I]; both machines receive the same inputs. */
interface MooreApplicative<I>: Applicative<K2<Moore.T, Unit, I>>, MooreFunctor<I> {
  override fun <A> pure(v: A): Moore<I, A> = Moore(v) { pure(v) }

  override fun <A, B> ap(f: K1<K2<Moore.T, Unit, I>, (A) -> B>, v: K1<K2<Moore.T, Unit, I>, A>): Moore<I, B> {
    val mf = Moore.narrow(f)
    val mv = Moore.narrow(v)
    return Moore(mf.output(mv.output)) { i -> ap(mf.step(i), mv.step(i)) }
  }
}
