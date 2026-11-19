package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Field

/**
 * Double field: ordinary addition, multiplication, negation and division.
 *
 * Floating point caveat: IEEE 754 arithmetic rounds, so associativity and distributivity hold only
 * approximately (e.g. `(0.1 + 0.2) + 0.3 != 0.1 + (0.2 + 0.3)`), `NaN` breaks every law, and
 * `reciprocal(zero())` is `Infinity`. Equality is that of boxed doubles, so `-0.0` differs from `0.0`
 * (e.g. `times(zero(), (-1.0).toField())` is `(-0.0).toField()`). The laws hold exactly only for values whose results are exactly
 * representable, such as small integers and dyadic fractions.
 *
 * ```kotlin
 * Field.div(DoubleField::class.java, 3.0.toField(), 2.0.toField()) // 1.5.toField()
 * ```
 */
data class DoubleField(val value: Double) : Field<Double, DoubleField> {
    /** Creates the zero element; used for reflective instance lookup. */
    constructor(): this(0.0)

    override fun plus(a: DoubleField, b: DoubleField) = (a.value + b.value).toField()
    override fun times(a: DoubleField, b: DoubleField) = (a.value * b.value).toField()
    override fun zero() = 0.0.toField()
    override fun one() = 1.0.toField()
    override fun negate(a: DoubleField) = (-a.value).toField()
    override fun minus(a: DoubleField, b: DoubleField) = (a.value - b.value).toField()
    override fun reciprocal(a: DoubleField) = (1.0 / a.value).toField()
    override fun div(a: DoubleField, b: DoubleField) = (a.value / b.value).toField()
}

/** Wraps this value in a [DoubleField]. */
fun Double.toField() = DoubleField(this)
