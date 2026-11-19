package com.github.kotlinz.kotlinz.data.ref

import com.github.kotlinz.kotlinz.data.io.IO
import java.util.concurrent.atomic.AtomicReference

/**
 * An atomic mutable reference whose reads and writes are [IO] effects.
 *
 * Backed by an [AtomicReference]; [update] and [modify] retry with compare-and-set, so the update
 * function may run more than once under contention and should be pure.
 *
 * ```kotlin
 * val program = Ref.of(0) bind { ref -> ref.update { it + 1 } bind { ref.get() } }
 * program.unsafeRun() // 1
 * ```
 */
class Ref<A> private constructor(private val cell: AtomicReference<A>) {
  companion object {
    /** Creates a new reference holding [initial]; each run of the returned [IO] allocates a fresh one. */
    fun <A> of(initial: A): IO<Ref<A>> = IO { unsafeOf(initial) }
    /** Creates a new reference immediately, outside of [IO]. */
    fun <A> unsafeOf(initial: A): Ref<A> = Ref(AtomicReference(initial))
  }

  /** Reads the current value. */
  fun get(): IO<A> = IO { cell.get() }
  /** Replaces the current value with [v]. */
  fun set(v: A): IO<Unit> = IO { cell.set(v) }
  /** Replaces the current value with [v] and returns the previous one. */
  fun getAndSet(v: A): IO<A> = IO { cell.getAndSet(v) }
  /** Atomically applies [f] to the current value. */
  fun update(f: (A) -> A): IO<Unit> = modify { a -> Pair(f(a), Unit) }
  /** Atomically applies [f] and returns the previous value. */
  fun getAndUpdate(f: (A) -> A): IO<A> = modify { a -> Pair(f(a), a) }
  /** Atomically applies [f] and returns the new value. */
  fun updateAndGet(f: (A) -> A): IO<A> = modify { a -> f(a).let { Pair(it, it) } }
  /** Atomically replaces the value with the first component of [f] and returns the second. */
  fun <B> modify(f: (A) -> Pair<A, B>): IO<B> = IO { modifyNow(f) }

  private tailrec fun <B> modifyNow(f: (A) -> Pair<A, B>): B {
    val current = cell.get()
    val (next, result) = f(current)
    return if (cell.compareAndSet(current, next)) result else modifyNow(f)
  }
}
