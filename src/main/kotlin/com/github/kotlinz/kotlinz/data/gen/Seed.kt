package com.github.kotlinz.kotlinz.data.gen

/**
 * An immutable SplitMix64 generator state.
 *
 * [next] returns a well-mixed 64-bit value and the following seed without mutating anything, so the
 * same seed always yields the same sequence. SplitMix64 is small, fast and passes BigCrush, which
 * makes it a good fit for a pure generator threaded through [com.github.kotlinz.kotlinz.data.state.State].
 *
 * @property value the raw 64-bit state.
 */
data class Seed(val value: Long) {
  /** Returns the next pseudo-random 64-bit value together with the advanced seed. */
  fun next(): Pair<Long, Seed> {
    val advanced = value + GOLDEN_GAMMA
    return Pair(mix(advanced), Seed(advanced))
  }

  private companion object {
    const val GOLDEN_GAMMA = -0x61c8864680b583ebL

    fun mix(v: Long): Long {
      val z1 = (v xor (v ushr 30)) * -0x40a7b892e31b1a47L
      val z2 = (z1 xor (z1 ushr 27)) * -0x6b2fb644ecceee15L
      return z2 xor (z2 ushr 31)
    }
  }
}
