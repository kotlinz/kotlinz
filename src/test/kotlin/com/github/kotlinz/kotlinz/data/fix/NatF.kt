package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

sealed class NatF<A>: K1<NatF.T, A> {
    class T

    class Zero<A>: NatF<A>() {
        override fun equals(other: Any?): Boolean = other is Zero<*>
        override fun hashCode(): Int = 0
    }

    data class Succ<A>(val prev: A): NatF<A>()

    companion object {
        fun <A> narrow(v: K1<T, A>): NatF<A> = v as NatF<A>
    }
}

interface NatFFunctor: Functor<NatF.T> {
    override fun <A, B> fmap(f: (A) -> B, v: K1<NatF.T, A>): NatF<B> = when (val nat = NatF.narrow(v)) {
        is NatF.Zero -> NatF.Zero()
        is NatF.Succ -> NatF.Succ(f(nat.prev))
    }
}
