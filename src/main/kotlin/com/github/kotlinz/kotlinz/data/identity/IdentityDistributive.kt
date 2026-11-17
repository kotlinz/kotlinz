package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.distributive.Distributive

interface IdentityDistributive: Distributive<Identity.T>, IdentityFunctor {
  override fun <F, A> distribute(functor: Functor<F>, fga: K1<F, K1<Identity.T, A>>): Identity<K1<F, A>> {
    return Identity(functor.fmap({ ga: K1<Identity.T, A> -> Identity.narrow(ga).value }, fga))
  }
}
