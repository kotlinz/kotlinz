package com.github.kotlinz.kotlinz.data.twin

import com.github.kotlinz.kotlinz.K1

/**
 * Two values of the same type, i.e. a container indexed by [Boolean].
 *
 * Use it as the simplest representable functor, or wherever a homogeneous pair needs to be mapped
 * uniformly. `false` indexes [first] and `true` indexes [second].
 *
 * Instances: [TwinFunctor], [TwinRepresentable].
 *
 * ```kotlin
 * val t = Twin(1, 2)
 * t.fmap { it * 10 }              // Twin(10, 20)
 * t.index(true)                   // 2
 * Twin.tabulate { if (it) "yes" else "no" }   // Twin("no", "yes")
 * ```
 */
data class Twin<A>(val first: A, val second: A): K1<Twin.T, A> {
  /** Higher-kinded witness for [Twin]. */
  class T

  companion object {
    /** Downcasts a higher-kinded value to [Twin]. */
    fun <A> narrow(v: K1<T, A>): Twin<A> = v as Twin<A>

    // Representable
    /** Builds a [Twin] from `f(false)` and `f(true)`. */
    fun <A> tabulate(f: (Boolean) -> A): Twin<A> = representable.tabulate(f)
    private val representable = object: TwinRepresentable {}
  }

  /** Swaps the two values. */
  fun swap(): Twin<A> = Twin(second, first)

  // Functor
  /** Maps both values. */
  infix fun <B> fmap(f: (A) -> B): Twin<B> = representable.fmap(f, this)

  // Representable
  /** Returns [second] for `true` and [first] for `false`. */
  fun index(rep: Boolean): A = representable.index(this)(rep)
  private val representable get() = object: TwinRepresentable {}
}
