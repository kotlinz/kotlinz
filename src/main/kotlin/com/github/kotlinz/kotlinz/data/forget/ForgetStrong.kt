package com.github.kotlinz.kotlinz.data.forget

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/** Strong instance for [Forget]: the extra component is discarded. */
interface ForgetStrong<R>: Strong<Forget.T, R>, ForgetProfunctor<R> {
  override fun <A, B, C> first(v: K3<Forget.T, R, A, B>): Forget<R, Pair<A, C>, Pair<B, C>> {
    val forget = Forget.narrow(v)
    return Forget { (a, _) -> forget.run(a) }
  }

  override fun <A, B, C> second(v: K3<Forget.T, R, A, B>): Forget<R, Pair<C, A>, Pair<C, B>> {
    val forget = Forget.narrow(v)
    return Forget { (_, a) -> forget.run(a) }
  }
}
