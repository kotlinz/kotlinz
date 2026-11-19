package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityApplicative
import com.github.kotlinz.kotlinz.data.identity.IdentityFunctor
import com.github.kotlinz.kotlinz.type.profunctor.Traversing
import com.github.kotlinz.kotlinz.type.profunctor.Wander

/** Traversing instance for [Function1]: maps every focus by running the traversal in `Identity`. */
interface Function1Traversing: Traversing<Function1.T, Unit>, Function1Strong, Function1Choice {
  override fun <S, R, A, B> wander(traversal: Wander<S, R, A, B>, v: K3<Function1.T, Unit, A, B>): Function1<S, R> {
    val function = Function1.narrow(v)
    val identity = object : IdentityApplicative, IdentityFunctor {}
    return Function1 { s -> Identity.narrow(traversal.run(identity, { a: A -> Identity(function.run(a)) }, s)).value }
  }
}
