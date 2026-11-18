package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either

/**
 * A monad that supports stack-safe monadic recursion.
 *
 * Law: `tailRecM(a, f)` is equivalent to the naive recursion
 * `bind({ e -> e.fold({ a2 -> tailRecM(a2, f) }, ::pure) }, f(a))`, but runs in constant stack space.
 *
 * @see Monad
 */
interface MonadRec<T>: Monad<T> {
  /** Repeatedly applies `f`, continuing on `Left` and stopping with the `Right` result. */
  fun <A, B> tailRecM(a: A, f: (A) -> K1<T, Either<A, B>>): K1<T, B>
}
