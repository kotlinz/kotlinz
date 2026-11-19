package com.github.kotlinz.kotlinz.data.resource

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * Monad instance for [Resource].
 *
 * `bind` releases the outer resource if acquiring the inner one fails, and otherwise releases
 * the inner resource before the outer one. Not stack-safe for very deep bind chains.
 */
interface ResourceMonad: Monad<Resource.T>, ResourceApplicative, ResourceFunctor {
  override fun <A> join(v: K1<Resource.T, K1<Resource.T, A>>): Resource<A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<Resource.T, B>, v: K1<Resource.T, A>): Resource<B> {
    return Resource(IO {
      val (a, releaseA) = Resource.narrow(v).allocate.unsafeRun()
      val (b, releaseB) = releaseOnFailure({ Resource.narrow(f(a)).allocate.unsafeRun() }, releaseA)
      Pair(b, IO { guarantee({ releaseB.unsafeRun() }, releaseA) })
    })
  }
}
