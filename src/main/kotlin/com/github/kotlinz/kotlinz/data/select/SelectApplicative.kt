package com.github.kotlinz.kotlinz.data.select

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Select]; the function is selected knowing which value it will be applied to. */
interface SelectApplicative<R>: Applicative<K1<Select.T, R>>, SelectFunctor<R> {
  override fun <A> pure(v: A): Select<R, A> = Select { v }

  override fun <A, B> ap(f: K1<K1<Select.T, R>, (A) -> B>, v: K1<K1<Select.T, R>, A>): Select<R, B> {
    val (selectf, select) = Pair(Select.narrow(f), Select.narrow(v))
    return Select { k ->
      val h = { g: (A) -> B -> g(select.run { a -> k(g(a)) }) }
      h(selectf.run { g -> k(h(g)) })
    }
  }
}
