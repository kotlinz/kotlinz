package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.Filterable

/** Filterable instance for [Chain], preserving the order of the kept elements. */
interface ChainFilterable: Filterable<Chain.T>, ChainFunctor {
  override fun <A, B> mapFilter(f: (A) -> Maybe<B>, v: K1<Chain.T, A>): Chain<B> =
    Chain.fromList(Chain.narrow(v).flatMap { a -> f(a).foldr(listOf<B>()) { b, acc -> listOf(b) + acc } })

  override fun <A> filter(p: (A) -> Boolean, v: K1<Chain.T, A>): Chain<A> = Chain.fromList(Chain.narrow(v).toList().filter(p))
  override fun <A, B: Any> collect(f: (A) -> B?, v: K1<Chain.T, A>): Chain<B> = Chain.fromList(Chain.narrow(v).toList().mapNotNull(f))
  override fun <A> catMaybes(v: K1<Chain.T, Maybe<A>>): Chain<A> = Chain.narrow(super.catMaybes(v))
}
