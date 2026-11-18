package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1

/**
 * Lifting helpers that turn plain functions into functions over an [Applicative].
 *
 * Law: `liftA2(f)(a, b) == ap(fmap(f.curried(), a), b)` (and likewise for the other arities).
 *
 * @see com.github.kotlinz.kotlinz.type.monad.MonadOps
 */
interface ApplicativeOps<T>: Applicative<T> {
  /** Lifts a unary function; equivalent to `fmap`. */
  fun <A, B> liftA(f: (A) -> B): (K1<T, A>) -> K1<T, B>
  /** Lifts a binary function over two independent effects. */
  fun <A, B, C> liftA2(f: (A, B) -> C): (K1<T, A>, K1<T, B>) -> K1<T, C>
  /** Lifts a ternary function over three independent effects. */
  fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<T, A>, K1<T, B>, K1<T, C>) -> K1<T, D>
}