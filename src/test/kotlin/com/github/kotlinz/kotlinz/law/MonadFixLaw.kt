package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Knot
import com.github.kotlinz.kotlinz.type.monad.MonadFix
import io.kotest.matchers.booleans.shouldBeTrue

class MonadFixLaw<T>(val monad: MonadFix<T>, val eq: (K1<T, *>, K1<T, *>) -> Boolean = { a, b -> a == b }) {
    fun <A> purity(h: (() -> A) -> A): Boolean =
            eq(monad.mfix { x -> monad.pure(h(x)) }, monad.pure(Knot.fix(h)))

    fun <A, B> leftShrinking(a: K1<T, B>, f: (() -> A, B) -> K1<T, A>): Boolean =
            eq(monad.mfix { x -> monad.bind({ y -> f(x, y) }, a) }, monad.bind({ y: B -> monad.mfix { x -> f(x, y) } }, a))

    fun <A> sliding(f: (() -> A) -> K1<T, A>, h: (A) -> A): Boolean =
            eq(monad.mfix { x -> monad.fmap(h, f(x)) }, monad.fmap(h, monad.mfix { x: () -> A -> f { h(x()) } }))

    fun <A> nesting(f: (() -> A, () -> A) -> K1<T, A>): Boolean =
            eq(monad.mfix { x -> monad.mfix { y -> f(x, y) } }, monad.mfix { x -> f(x, x) })

    fun <A, B> assertSatisfyingMonadFixLaw(
        h: (() -> A) -> A, a: K1<T, B>, shrink: (() -> A, B) -> K1<T, A>,
        slide: (() -> A) -> K1<T, A>, strict: (A) -> A, nest: (() -> A, () -> A) -> K1<T, A>
    ) {
        purity(h).shouldBeTrue()
        leftShrinking(a, shrink).shouldBeTrue()
        sliding(slide, strict).shouldBeTrue()
        nesting(nest).shouldBeTrue()
    }
}
