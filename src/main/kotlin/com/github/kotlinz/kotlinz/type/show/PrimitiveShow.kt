package com.github.kotlinz.kotlinz.type.show

/** [Show] instance for [Int]. */
val Int.Companion.show: Show<Int> get() = Show.fromToString()
/** [Show] instance for [Long]. */
val Long.Companion.show: Show<Long> get() = Show.fromToString()
/** [Show] instance for [Short]. */
val Short.Companion.show: Show<Short> get() = Show.fromToString()
/** [Show] instance for [Byte]. */
val Byte.Companion.show: Show<Byte> get() = Show.fromToString()
/** [Show] instance for [Double]. */
val Double.Companion.show: Show<Double> get() = Show.fromToString()
/** [Show] instance for [Float]. */
val Float.Companion.show: Show<Float> get() = Show.fromToString()
/** [Show] instance for [Boolean]. */
val Boolean.Companion.show: Show<Boolean> get() = Show.fromToString()
/** [Show] instance for [Char], rendered in single quotes (e.g. `'a'`). */
val Char.Companion.show: Show<Char> get() = Show.by { "'" + escape(it.toString(), '\'') + "'" }
/** [Show] instance for [String], rendered in double quotes with `\` and `"` escaped (e.g. `"a"`). */
val String.Companion.show: Show<String> get() = Show.by { "\"" + escape(it, '"') + "\"" }

private fun escape(s: String, quote: Char): String = s.replace("\\", "\\\\").replace("$quote", "\\$quote")
