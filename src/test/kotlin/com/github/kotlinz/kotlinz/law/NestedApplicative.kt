package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

class NestedT<F, G>

data class Nested<F, G, A>(val value: K1<F, K1<G, A>>): K1<NestedT<F, G>, A> {
    companion object {
        fun <F, G, A> narrow(v: K1<NestedT<F, G>, A>): Nested<F, G, A> = v as Nested<F, G, A>
    }
}

class NestedApplicative<F, G>(val outer: Applicative<F>, val inner: Applicative<G>): Applicative<NestedT<F, G>> {
    override fun <A> pure(v: A): Nested<F, G, A> = Nested(outer.pure(inner.pure(v)))

    override fun <A, B> fmap(f: (A) -> B, v: K1<NestedT<F, G>, A>): Nested<F, G, B> =
            Nested(outer.fmap({ ga: K1<G, A> -> inner.fmap(f, ga) }, Nested.narrow(v).value))

    override fun <A, B> ap(f: K1<NestedT<F, G>, (A) -> B>, v: K1<NestedT<F, G>, A>): Nested<F, G, B> {
        val apInner = { gf: K1<G, (A) -> B> -> { ga: K1<G, A> -> inner.ap(gf, ga) } }
        return Nested(outer.ap(outer.fmap(apInner, Nested.narrow(f).value), Nested.narrow(v).value))
    }
}
