package com.github.kotlinz.kotlinz.data.tambara

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Tambara]: maps the first components, leaving the context untouched. */
interface TambaraProfunctor<PT, PF>: Profunctor<Tambara.T, Tambara.Of<PT, PF>> {
  /** The underlying profunctor. */
  val profunctor: Profunctor<PT, PF>

  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Tambara.T, Tambara.Of<PT, PF>, B, C>): Tambara<PT, PF, A, D> {
    val tambara = Tambara.narrow(v)
    return object: Tambara<PT, PF, A, D>() {
      override fun <E> run(): K3<PT, PF, Pair<A, E>, Pair<D, E>> =
        profunctor.dimap({ p: Pair<A, E> -> Pair(f(p.first), p.second) }, { p: Pair<C, E> -> Pair(g(p.first), p.second) }, tambara.run<E>())
    }
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Tambara.T, Tambara.Of<PT, PF>, B, C>): Tambara<PT, PF, A, C> = Tambara.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Tambara.T, Tambara.Of<PT, PF>, A, B>): Tambara<PT, PF, A, C> = Tambara.narrow(super.rmap(g, v))
}
