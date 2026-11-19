package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K3

/**
 * A functor over an indexed type constructor `T<I, J, A>`, where `I` and `J` are the
 * input and output indices (e.g. the state types before and after a step).
 *
 * Laws:
 * - Identity: `imap(id, v) == v`
 * - Composition: `imap(g . f, v) == imap(g, imap(f, v))`
 *
 * @see com.github.kotlinz.kotlinz.type.monad.IxMonad
 */
interface IxFunctor<T> {
  /** Applies `f` to the value inside `v`, keeping both indices. */
  fun <I, J, A, B> imap(f: (A) -> B, v: K3<T, I, J, A>): K3<T, I, J, B>
}
