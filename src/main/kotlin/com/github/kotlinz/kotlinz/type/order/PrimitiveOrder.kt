package com.github.kotlinz.kotlinz.type.order

// Instances delegate to `compareTo`; for floating point this is a total order where `-0.0 < 0.0` and `NaN` is the largest value.

/** [Order] instance for [Int]. */
val Int.Companion.order: Order<Int> get() = Order.fromComparable()
/** [Order] instance for [Long]. */
val Long.Companion.order: Order<Long> get() = Order.fromComparable()
/** [Order] instance for [Short]. */
val Short.Companion.order: Order<Short> get() = Order.fromComparable()
/** [Order] instance for [Byte]. */
val Byte.Companion.order: Order<Byte> get() = Order.fromComparable()
/** [Order] instance for [Double]. */
val Double.Companion.order: Order<Double> get() = Order.fromComparable()
/** [Order] instance for [Float]. */
val Float.Companion.order: Order<Float> get() = Order.fromComparable()
/** [Order] instance for [Char]. */
val Char.Companion.order: Order<Char> get() = Order.fromComparable()
/** [Order] instance for [Boolean]. */
val Boolean.Companion.order: Order<Boolean> get() = Order.fromComparable()
/** [Order] instance for [String]. */
val String.Companion.order: Order<String> get() = Order.fromComparable()
