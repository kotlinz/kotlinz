package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3

/**
 * A profunctor that can be lifted through functions from a fixed environment (the basis of grates).
 *
 * Laws:
 * - `lmap({ g -> g compose f }, closed(v)) == rmap({ g -> g compose f }, closed(v))`
 * - `closed(closed(v)) == dimap(::uncurry, ::curry, closed(v))`
 * - `dimap({ a -> { _ -> a } }, { g -> g(Unit) }, closed(v)) == v`
 *
 * ```kotlin
 * val closed = object : Function1Closed {}
 * closed.closed<Int, Int, String>(Function1 { n: Int -> n + 1 })({ s: String -> s.length })("abc") // 4
 * ```
 *
 * @see Profunctor
 */
interface Closed<T, F> : Profunctor<T, F> {
  /** Lifts `v` to post-compose on functions from any input `X`. */
  fun <A, B, X> closed(v: K3<T, F, A, B>): K3<T, F, (X) -> A, (X) -> B>
}
