package com.github.kotlinz.kotlinz.data.select

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Select]; the first choice is made by ranking each candidate's full continuation. */
interface SelectMonad<R>: Monad<K1<Select.T, R>>, SelectApplicative<R>, SelectFunctor<R> {
  override fun <A> join(v: K1<K1<Select.T, R>, K1<K1<Select.T, R>, A>>): Select<R, A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<K1<Select.T, R>, B>, v: K1<K1<Select.T, R>, A>): Select<R, B> {
    val select = Select.narrow(v)
    return Select { k ->
      val h = { a: A -> Select.narrow(f(a)).run(k) }
      h(select.run { a -> k(h(a)) })
    }
  }
}
