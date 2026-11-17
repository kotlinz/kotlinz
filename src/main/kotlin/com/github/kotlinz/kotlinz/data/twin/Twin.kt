package com.github.kotlinz.kotlinz.data.twin

import com.github.kotlinz.kotlinz.K1

data class Twin<A>(val first: A, val second: A): K1<Twin.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): Twin<A> = v as Twin<A>

    // Representable
    fun <A> tabulate(f: (Boolean) -> A): Twin<A> = representable.tabulate(f)
    private val representable = object: TwinRepresentable {}
  }

  fun swap(): Twin<A> = Twin(second, first)

  // Functor
  infix fun <B> fmap(f: (A) -> B): Twin<B> = representable.fmap(f, this)

  // Representable
  fun index(rep: Boolean): A = representable.index(this)(rep)
  private val representable get() = object: TwinRepresentable {}
}
