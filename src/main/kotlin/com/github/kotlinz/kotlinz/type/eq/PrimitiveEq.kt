package com.github.kotlinz.kotlinz.type.eq

// Instances delegate to boxed `equals`, so `Double.NaN` equals itself and `0.0` differs from `-0.0`.

/** [Eq] instance for [Int]. */
val Int.Companion.eq: Eq<Int> get() = Eq.fromEquals()
/** [Eq] instance for [Long]. */
val Long.Companion.eq: Eq<Long> get() = Eq.fromEquals()
/** [Eq] instance for [Short]. */
val Short.Companion.eq: Eq<Short> get() = Eq.fromEquals()
/** [Eq] instance for [Byte]. */
val Byte.Companion.eq: Eq<Byte> get() = Eq.fromEquals()
/** [Eq] instance for [Double]. */
val Double.Companion.eq: Eq<Double> get() = Eq.fromEquals()
/** [Eq] instance for [Float]. */
val Float.Companion.eq: Eq<Float> get() = Eq.fromEquals()
/** [Eq] instance for [Char]. */
val Char.Companion.eq: Eq<Char> get() = Eq.fromEquals()
/** [Eq] instance for [Boolean]. */
val Boolean.Companion.eq: Eq<Boolean> get() = Eq.fromEquals()
/** [Eq] instance for [String]. */
val String.Companion.eq: Eq<String> get() = Eq.fromEquals()
