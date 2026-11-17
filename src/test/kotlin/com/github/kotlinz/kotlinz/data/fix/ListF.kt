package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

sealed class ListF<E, A>: K2<ListF.T, E, A> {
    class T

    class Nil<E, A>: ListF<E, A>() {
        override fun equals(other: Any?): Boolean = other is Nil<*, *>
        override fun hashCode(): Int = 0
    }

    data class Cons<E, A>(val head: E, val tail: A): ListF<E, A>()

    companion object {
        fun <E, A> narrow(v: K1<K1<T, E>, A>): ListF<E, A> = v as ListF<E, A>
    }
}

interface ListFFunctor<E>: Functor<K1<ListF.T, E>> {
    override fun <A, B> fmap(f: (A) -> B, v: K1<K1<ListF.T, E>, A>): ListF<E, B> = when (val list = ListF.narrow(v)) {
        is ListF.Nil -> ListF.Nil()
        is ListF.Cons -> ListF.Cons(list.head, f(list.tail))
    }
}

fun <E> fixListOf(vararg values: E): Fix<K1<ListF.T, E>> =
    values.foldRight(Fix(ListF.Nil())) { head, tail -> Fix(ListF.Cons(head, tail)) }
