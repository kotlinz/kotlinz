package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.MonadRec

/**
 * MonadRec instance for [State].
 *
 * `tailRecM` iterates instead of recursing, so loops are stack-safe as long as each step is shallow.
 */
interface StateMonadRec<S>: MonadRec<K1<State.T, S>>, StateMonad<S> {
  override fun <A, B> tailRecM(a: A, f: (A) -> K1<K1<State.T, S>, Either<A, B>>): State<S, B> {
    return State { s ->
      val first = State.narrow(f(a)).run(s)
      val (last, end) = generateSequence(first) { (e, next) ->
        (e as? Either.Left)?.let { State.narrow(f(it.value)).run(next) }
      }.last()
      Pair((last as Either.Right).value, end)
    }
  }
}
