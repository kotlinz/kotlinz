package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.applicative.IxApplicative

/**
 * An indexed monad: a monad whose type index changes from `I` to `J` along a computation,
 * e.g. a state whose type changes ([com.github.kotlinz.kotlinz.data.indexedstate.IndexedState]).
 *
 * Indexed type constructors are encoded as `K3<T, I, J, A>`; transformers partially apply the
 * inner monad into the tag (`K3<K1<T, F>, I, J, A>`).
 *
 * Laws:
 * - Left identity: `ibind(f, ipure(a)) == f(a)`
 * - Right identity: `ibind(::ipure, m) == m`
 * - Associativity: `ibind(g, ibind(f, m)) == ibind({ a -> ibind(g, f(a)) }, m)`
 *
 * @see Monad
 */
interface IxMonad<T>: IxApplicative<T> {
  /** Sequences `v` (`I -> J`) with `f` (`J -> K`). */
  fun <I, J, K, A, B> ibind(f: (A) -> K3<T, J, K, B>, v: K3<T, I, J, A>): K3<T, I, K, B>
  /** Flattens one level of nesting. */
  fun <I, J, K, A> ijoin(v: K3<T, I, J, K3<T, J, K, A>>): K3<T, I, K, A> = ibind({ it }, v)
}
