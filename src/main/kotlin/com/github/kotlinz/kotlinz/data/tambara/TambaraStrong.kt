package com.github.kotlinz.kotlinz.data.tambara

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/** Strong instance for [Tambara], available for any underlying profunctor: nests the extra component into the context. */
interface TambaraStrong<PT, PF>: Strong<Tambara.T, Tambara.Of<PT, PF>>, TambaraProfunctor<PT, PF> {
  override fun <A, B, C> first(v: K3<Tambara.T, Tambara.Of<PT, PF>, A, B>): Tambara<PT, PF, Pair<A, C>, Pair<B, C>> {
    val tambara = Tambara.narrow(v)
    return object: Tambara<PT, PF, Pair<A, C>, Pair<B, C>>() {
      override fun <E> run(): K3<PT, PF, Pair<Pair<A, C>, E>, Pair<Pair<B, C>, E>> =
        profunctor.dimap(::assoc, ::unassoc, tambara.run<Pair<C, E>>())
    }
  }

  override fun <A, B, C> second(v: K3<Tambara.T, Tambara.Of<PT, PF>, A, B>): Tambara<PT, PF, Pair<C, A>, Pair<C, B>> =
    Tambara.narrow(super.second(v))
}

private fun <X, Y, Z> assoc(p: Pair<Pair<X, Y>, Z>): Pair<X, Pair<Y, Z>> = Pair(p.first.first, Pair(p.first.second, p.second))
private fun <X, Y, Z> unassoc(p: Pair<X, Pair<Y, Z>>): Pair<Pair<X, Y>, Z> = Pair(Pair(p.first, p.second.first), p.second.second)
