package com.github.kotlinz.kotlinz.data.resource

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Resource]; the function resource is acquired first and released last. */
interface ResourceApplicative: Applicative<Resource.T>, ResourceFunctor {
  override fun <A> pure(v: A): Resource<A> = Resource(IO { Pair(v, IO.pure(Unit)) })

  override fun <A, B> ap(f: K1<Resource.T, (A) -> B>, v: K1<Resource.T, A>): Resource<B> {
    return Resource.narrow(f) bind { g -> Resource.narrow(v) fmap g }
  }
}
