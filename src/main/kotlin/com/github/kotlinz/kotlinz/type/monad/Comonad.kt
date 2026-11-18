package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/**
 * The dual of a monad: a context from which a value can always be extracted and which can be
 * extended with context-dependent computations.
 *
 * Laws:
 * - `extract(duplicate(v)) == v`
 * - `fmap(::extract, duplicate(v)) == v`
 * - `duplicate(duplicate(v)) == fmap(::duplicate, duplicate(v))`
 *
 * @see com.github.kotlinz.kotlinz.type.pointed.Copointed
 * @see Monad
 */
interface Comonad<T>: Copointed<T>, Functor<T> {
  /** Wraps every focus position of `v` with the whole context focused there. */
  fun <A> duplicate(v: K1<T, A>): K1<T, K1<T, A>>
  /** Applies a context-consuming function at every position of `v`. */
  fun <A, B> extend(f: (K1<T, A>) -> B, v: K1<T, A>): K1<T, B> = fmap(f, duplicate(v))
}