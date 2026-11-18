package com.github.kotlinz.kotlinz.data.day

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Day]. Requires no instances of F or G. */
interface DayFunctor<F, G>: Functor<K2<Day.T, F, G>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Day.T, F, G>, A>): Day<F, G, B> {
    val day = Day.narrow(v)
    return Day(day.left, day.right) { b: Any?, c: Any? -> f(day.combine(b, c)) }
  }
}
