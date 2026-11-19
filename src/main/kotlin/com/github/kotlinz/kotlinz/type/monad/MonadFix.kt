package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A monad with value recursion: [mfix] lets a computation refer to its own (future) result.
 *
 * Kotlin is strict, so the recursive value is passed as a thunk. The thunk may be captured in the
 * result (e.g. in a lazily linked structure) but must not be forced before `f` has produced its
 * result; forcing it too early throws [IllegalStateException] (the strict counterpart of a diverging
 * `mfix` in Haskell).
 *
 * Laws (with `fix(h)` the lazy fixed point of `h: (() -> A) -> A`):
 * - Purity: `mfix { x -> pure(h(x)) } == pure(fix(h))`
 * - Left shrinking: `mfix { x -> bind({ y -> f(x, y) }, a) } == bind({ y -> mfix { x -> f(x, y) } }, a)`
 * - Sliding: `mfix { x -> fmap(h, f(x)) } == fmap(h, mfix { x -> f { h(x()) } })` for strict `h`
 * - Nesting: `mfix { x -> mfix { y -> f(x, y) } } == mfix { x -> f(x, x) }`
 *
 * @see Monad
 * @see com.github.kotlinz.kotlinz.type.arrow.ArrowLoop
 */
interface MonadFix<T>: Monad<T> {
  /** Computes the fixed point of `f`, passing it a thunk that yields `f`'s own result. */
  fun <A> mfix(f: (() -> A) -> K1<T, A>): K1<T, A>
}
