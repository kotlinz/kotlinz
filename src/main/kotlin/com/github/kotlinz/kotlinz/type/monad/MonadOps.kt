package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * Lifting helpers that turn plain functions into functions over a [Monad], sequencing effects left to right.
 *
 * Law: `liftM2(f)(a, b) == bind({ x -> fmap({ y -> f(x, y) }, b) }, a)` (and likewise for the other arities).
 *
 * @see com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps
 */
interface MonadOps<T>: Monad<T> {
  /** Lifts a unary function; equivalent to `fmap`. */
  fun <A, B> liftM(f: (A) -> B): (K1<T, A>) -> K1<T, B>
  /** Lifts a binary function, running `m1` before `m2`. */
  fun <A, B, C> liftM2(f: (A, B) -> C): (K1<T, A>, K1<T, B>) -> K1<T, C>
  /** Lifts a ternary function, running the arguments left to right. */
  fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<T, A>, K1<T, B>, K1<T, C>) -> K1<T, D>
}
