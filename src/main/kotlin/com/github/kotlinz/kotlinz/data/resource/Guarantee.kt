package com.github.kotlinz.kotlinz.data.resource

import com.github.kotlinz.kotlinz.data.io.IO

internal fun <A> guarantee(body: () -> A, release: IO<Unit>): A {
  val result = runCatching(body)
  val released = runCatching { release.unsafeRun() }
  return result.getOrElse { e -> throw e.withSuppressed(released.exceptionOrNull()) }.also { released.getOrThrow() }
}

internal fun <A> releaseOnFailure(body: () -> A, release: IO<Unit>): A {
  return runCatching(body).getOrElse { e -> guarantee({ throw e }, release) }
}

private fun Throwable.withSuppressed(other: Throwable?): Throwable = apply {
  other?.takeIf { it !== this }?.let(::addSuppressed)
}
