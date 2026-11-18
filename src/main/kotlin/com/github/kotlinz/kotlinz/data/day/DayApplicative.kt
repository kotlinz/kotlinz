package com.github.kotlinz.kotlinz.data.day

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Day]. Requires Applicatives of F and G. */
interface DayApplicative<F, G>: Applicative<K2<Day.T, F, G>>, DayFunctor<F, G> {
  /** Applicative of the left functor. */
  val first: Applicative<F>
  /** Applicative of the right functor. */
  val second: Applicative<G>

  companion object {
    /** Creates the instance from Applicatives of F and G. */
    fun <F, G> of(first: Applicative<F>, second: Applicative<G>): DayApplicative<F, G> {
      val (f, g) = Pair(first, second)
      return object: DayApplicative<F, G> {
        override val first = f
        override val second = g
      }
    }
  }

  override fun <A> pure(v: A): Day<F, G, A> = Day(first.pure(Unit), second.pure(Unit)) { _: Unit, _: Unit -> v }

  override fun <A, B> ap(f: K1<K2<Day.T, F, G>, (A) -> B>, v: K1<K2<Day.T, F, G>, A>): Day<F, G, B> {
    val (df, dv) = Pair(Day.narrow(f), Day.narrow(v))
    return Day(zip(first, df.left, dv.left), zip(second, df.right, dv.right)) { b: Pair<Any?, Any?>, c: Pair<Any?, Any?> ->
      df.combine(b.first, c.first)(dv.combine(b.second, c.second))
    }
  }
}

private fun <F, A, B> zip(applicative: Applicative<F>, fa: K1<F, A>, fb: K1<F, B>): K1<F, Pair<A, B>> =
  applicative.ap(applicative.fmap({ a: A -> { b: B -> Pair(a, b) } }, fa), fb)
