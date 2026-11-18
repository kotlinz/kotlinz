package com.github.kotlinz.kotlinz.data.day

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [Day]. Requires Comonads of F and G. */
interface DayComonad<F, G>: Comonad<K2<Day.T, F, G>>, DayFunctor<F, G> {
  /** Comonad of the left functor. */
  val first: Comonad<F>
  /** Comonad of the right functor. */
  val second: Comonad<G>

  companion object {
    /** Creates the instance from Comonads of F and G. */
    fun <F, G> of(first: Comonad<F>, second: Comonad<G>): DayComonad<F, G> {
      val (f, g) = Pair(first, second)
      return object: DayComonad<F, G> {
        override val first = f
        override val second = g
      }
    }
  }

  override fun <A> extract(v: K1<K2<Day.T, F, G>, A>): A {
    val day = Day.narrow(v)
    return day.combine(first.extract(day.left), second.extract(day.right))
  }

  override fun <A> duplicate(v: K1<K2<Day.T, F, G>, A>): Day<F, G, K1<K2<Day.T, F, G>, A>> {
    val day = Day.narrow(v)
    return Day(first.duplicate(day.left), second.duplicate(day.right)) { fb: K1<F, Any?>, gc: K1<G, Any?> ->
      Day(fb, gc, day.combine)
    }
  }
}
