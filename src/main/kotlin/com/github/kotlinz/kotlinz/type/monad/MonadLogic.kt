package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe

/**
 * A [MonadPlus] whose alternatives can be inspected one at a time, enabling fair and pruned search.
 *
 * Minimal definition is [msplit]; the other operations are derived from it but may be overridden.
 *
 * Laws:
 * - `msplit(mzero()) == pure(None)`
 * - `msplit(mplus(pure(a), m)) == pure(Just(a to m))`
 *
 * @see MonadPlus
 */
interface MonadLogic<T>: MonadPlus<T> {
  /** Splits off the first result, returning it with the remaining computation, or `None` if there is none. */
  fun <A> msplit(v: K1<T, A>): K1<T, Maybe<Pair<A, K1<T, A>>>>

  /** Fair disjunction: alternates results of `v1` and `v2`, so an infinite `v1` cannot starve `v2`. */
  fun <A> interleave(v1: K1<T, A>, v2: K1<T, A>): K1<T, A> = bind({ split: Maybe<Pair<A, K1<T, A>>> ->
    when (split) {
      is Maybe.Just -> mplus(pure(split.value.first), interleave(v2, split.value.second))
      is Maybe.None -> v2
    }
  }, msplit(v1))

  /** Fair conjunction: like [bind], but interleaves the results produced for each value of `v`. */
  fun <A, B> fairBind(f: (A) -> K1<T, B>, v: K1<T, A>): K1<T, B> = bind({ split: Maybe<Pair<A, K1<T, A>>> ->
    when (split) {
      is Maybe.Just -> interleave(f(split.value.first), fairBind(f, split.value.second))
      is Maybe.None -> mzero()
    }
  }, msplit(v))

  /** Soft cut: binds every result of `cond` to `then`, or runs `otherwise` if `cond` has no result. */
  fun <A, B> ifte(cond: K1<T, A>, then: (A) -> K1<T, B>, otherwise: K1<T, B>): K1<T, B> =
    bind({ split: Maybe<Pair<A, K1<T, A>>> ->
      when (split) {
        is Maybe.Just -> mplus(then(split.value.first), bind(then, split.value.second))
        is Maybe.None -> otherwise
      }
    }, msplit(cond))

  /** Pruning: keeps only the first result of `v`. */
  fun <A> once(v: K1<T, A>): K1<T, A> = bind({ split: Maybe<Pair<A, K1<T, A>>> ->
    when (split) {
      is Maybe.Just -> pure(split.value.first)
      is Maybe.None -> mzero()
    }
  }, msplit(v))

  /** Negation as failure: succeeds once with `Unit` exactly when `v` has no result. */
  fun <A> lnot(v: K1<T, A>): K1<T, Unit> = ifte(once(v), { mzero() }, pure(Unit))
}
