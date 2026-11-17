package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.matchers.booleans.shouldBeTrue

class NaturalityLaw<F, G>(
    val nt: NaturalTransformation<F, G>,
    val from: Functor<F>,
    val to: Functor<G>,
    val eq: (K1<G, *>, K1<G, *>) -> Boolean = { a, b -> a == b }
) {
    fun <A, B> naturality(fa: K1<F, A>, f: (A) -> B): Boolean =
            eq(to.fmap(f, nt(fa)), nt(from.fmap(f, fa)))

    fun <A, B> assertSatisfyingNaturalityLaw(fa: K1<F, A>, f: (A) -> B) {
        naturality(fa, f).shouldBeTrue()
    }
}
