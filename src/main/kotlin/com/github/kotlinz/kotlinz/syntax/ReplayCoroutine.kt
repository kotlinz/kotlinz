package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.startCoroutine

internal class ReplayCoroutine<T, A>(
  private val monad: Monad<T>,
  private val block: suspend MonadScope<T>.() -> A,
  private val history: List<Any?>
) : MonadScope<T>, Continuation<A> {
  private var index = 0
  private lateinit var result: K1<T, A>

  override val context = EmptyCoroutineContext

  fun run(): K1<T, A> {
    block.startCoroutine(this, this)
    return result
  }

  override fun resumeWith(result: Result<A>) {
    this.result = monad.pure(result.getOrThrow())
  }

  @Suppress("UNCHECKED_CAST")
  override suspend fun <B> K1<T, B>.bind(): B {
    if (index < history.size) return history[index++] as B
    return suspendCoroutineUninterceptedOrReturn { _ ->
      result = monad.bind({ value: B -> replayWith(value) }, this)
      COROUTINE_SUSPENDED
    }
  }

  private fun replayWith(value: Any?): K1<T, A> = ReplayCoroutine(monad, block, history + listOf(value)).run()
}
