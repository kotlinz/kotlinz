package com.github.kotlinz.kotlinz.data.const

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.Selective
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Selective instance for [Const]. It cannot look inside the (phantom) result, so [select] always
 * combines both held values (`fab.value <> fn.value`): an over-approximation of the effects, which
 * makes `Const` useful for statically collecting every effect a selective computation may perform.
 */
interface ConstSelective<S: Monoid<*, S>>: Selective<K1<Const.T, S>>, ConstApplicative<S> {
  override fun <A, B> select(fab: K1<K1<Const.T, S>, Either<A, B>>, fn: K1<K1<Const.T, S>, (A) -> B>): Const<S, B> =
    Const(Monoid.op(type, Const.narrow(fab).value, Const.narrow(fn).value))
}
