package com.github.kotlinz.kotlinz.data.resource

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A resource that is safely acquired and released around its use, built on [IO].
 *
 * [allocate] acquires the value together with the [IO] that releases it. Composing resources with
 * `bind` acquires them in order and releases them in reverse order; releases always run, even when
 * a later acquisition, the use, or another release fails. When a release fails after a failure,
 * its exception is attached to the original one as suppressed.
 *
 * Instances: [ResourceFunctor], [ResourceApplicative], [ResourceMonad].
 *
 * ```kotlin
 * val file = Resource.make(IO { open("a.txt") }) { f -> IO { f.close() } }
 * val both = file bind { a -> Resource.make(IO { open("b.txt") }) { b -> IO { b.close() } } fmap { b -> a to b } }
 * both.use { (a, b) -> IO { copy(a, b) } }.unsafeRun() // closes b, then a
 * ```
 *
 * @property allocate acquires the value and returns it with its release action.
 */
class Resource<A>(val allocate: IO<Pair<A, IO<Unit>>>): K1<Resource.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Resource]. */
    fun <A> narrow(v: K1<T, A>): Resource<A> = v as Resource<A>

    /** Creates a resource from an [acquire] effect and a [release] function for the acquired value. */
    fun <A> make(acquire: IO<A>, release: (A) -> IO<Unit>): Resource<A> = Resource(acquire fmap { a -> Pair(a, release(a)) })
    /** Lifts an [IO] into a resource that needs no release. */
    fun <A> liftIO(io: IO<A>): Resource<A> = Resource(io fmap { a -> Pair(a, IO.pure(Unit)) })

    // Monad
    /** Wraps a pure value into a resource that needs no release. */
    fun <A> pure(v: A): Resource<A> = monad.pure(v)
    /** Flattens a nested [Resource]. */
    fun <A> join(v: K1<T, K1<T, A>>): Resource<A> = monad.join(v)
    private val monad = object: ResourceMonad {}
    /** Builds a [Resource] with monad comprehension syntax. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Resource<A> = narrow(monad.fx(block))
  }

  // Monad
  /** Maps the acquired value. */
  infix fun <B> fmap(f: (A) -> B): Resource<B> = monad.fmap(f, this)
  /** Acquires [f] then this resource, and applies the function to the value. */
  infix fun <B> ap(f: Resource<(A) -> B>): Resource<B> = monad.ap(f, this)
  /** Acquires this resource, then the resource produced by [f]; they are released in reverse order. */
  infix fun <B> bind(f: (A) -> Resource<B>): Resource<B> = monad.bind(f, this)
  private val monad = object: ResourceMonad {}

  /** Acquires the resource, runs [f] with it and releases it afterwards, whether [f] succeeds or fails. */
  fun <B> use(f: (A) -> IO<B>): IO<B> = IO {
    val (a, release) = allocate.unsafeRun()
    guarantee({ f(a).unsafeRun() }, release)
  }
}
