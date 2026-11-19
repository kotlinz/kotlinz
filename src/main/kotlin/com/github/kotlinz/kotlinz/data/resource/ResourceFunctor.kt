package com.github.kotlinz.kotlinz.data.resource

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Resource]; maps the acquired value and keeps its release action. */
interface ResourceFunctor: Functor<Resource.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Resource.T, A>): Resource<B> {
    return Resource(Resource.narrow(v).allocate fmap { (a, release) -> Pair(f(a), release) })
  }
}
