package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine

internal class FxCoroutine<T, A>(
  private val monad: Monad<T>,
  private val block: suspend MonadScope<T>.() -> A
) : MonadScope<T>, Continuation<A> {
  private lateinit var result: K1<T, A>

  override val context = EmptyCoroutineContext

  fun run(): K1<T, A> {
    block.startCoroutine(this, this)
    return result
  }

  override fun resumeWith(result: Result<A>) {
    this.result = monad.pure(result.getOrThrow())
  }

  override suspend fun <B> K1<T, B>.bind(): B = suspendCoroutineUninterceptedOrReturn { continuation ->
    result = monad.bind({ value: B -> continuation.resume(value); result }, this)
    COROUTINE_SUSPENDED
  }
}
