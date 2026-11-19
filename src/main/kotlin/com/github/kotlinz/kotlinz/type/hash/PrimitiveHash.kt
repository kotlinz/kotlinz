package com.github.kotlinz.kotlinz.type.hash

// Instances delegate to boxed `equals` / `hashCode`, matching the primitive `Eq` instances.

/** [Hash] instance for [Int]. */
val Int.Companion.hash: Hash<Int> get() = Hash.fromHashCode()
/** [Hash] instance for [Long]. */
val Long.Companion.hash: Hash<Long> get() = Hash.fromHashCode()
/** [Hash] instance for [Short]. */
val Short.Companion.hash: Hash<Short> get() = Hash.fromHashCode()
/** [Hash] instance for [Byte]. */
val Byte.Companion.hash: Hash<Byte> get() = Hash.fromHashCode()
/** [Hash] instance for [Double]. */
val Double.Companion.hash: Hash<Double> get() = Hash.fromHashCode()
/** [Hash] instance for [Float]. */
val Float.Companion.hash: Hash<Float> get() = Hash.fromHashCode()
/** [Hash] instance for [Char]. */
val Char.Companion.hash: Hash<Char> get() = Hash.fromHashCode()
/** [Hash] instance for [Boolean]. */
val Boolean.Companion.hash: Hash<Boolean> get() = Hash.fromHashCode()
/** [Hash] instance for [String]. */
val String.Companion.hash: Hash<String> get() = Hash.fromHashCode()
