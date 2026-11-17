package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.representable.Representable
import io.kotest.matchers.booleans.shouldBeTrue

class RepresentableLaw<F, Rep>(
    val representable: Representable<F, Rep>,
    val reps: List<Rep>,
    val eq: (K1<F, *>, K1<F, *>) -> Boolean = { a, b -> a == b }
) {
    fun <A> indexTabulate(f: (Rep) -> A): Boolean =
            reps.all { representable.index(representable.tabulate(f))(it) == f(it) }

    fun <A> tabulateIndex(fa: K1<F, A>): Boolean =
            eq(representable.tabulate(representable.index(fa)), fa)

    fun <A> assertSatisfyingRepresentableLaw(f: (Rep) -> A, fa: K1<F, A>) {
        indexTabulate(f).shouldBeTrue()
        tabulateIndex(fa).shouldBeTrue()
    }
}
