package com.github.kotlinz.kotlinz.data.op

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Contravariant

/** Contravariant instance for [Op] with a fixed result type `R`. */
interface OpContravariant<R>: Contravariant<K1<Op.T, R>> {
  override fun <A, B> contramap(f: (B) -> A, v: K1<K1<Op.T, R>, A>): Op<R, B> =
    Op { b -> Op.narrow(v).run(f(b)) }
}
