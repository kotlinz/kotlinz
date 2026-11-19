package com.github.kotlinz.kotlinz.data.forget

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Forget]: only the input is mapped, the output is phantom. */
interface ForgetProfunctor<R>: Profunctor<Forget.T, R> {
  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Forget.T, R, B, C>): Forget<R, A, D> {
    val forget = Forget.narrow(v)
    return Forget { a -> forget.run(f(a)) }
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Forget.T, R, B, C>): Forget<R, A, C> = Forget.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Forget.T, R, A, B>): Forget<R, A, C> = Forget.narrow(super.rmap(g, v))
}
