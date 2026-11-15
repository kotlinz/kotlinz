package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.K1
import kotlin.coroutines.RestrictsSuspension

@RestrictsSuspension
interface MonadScope<T> {
  suspend fun <A> K1<T, A>.bind(): A
}
