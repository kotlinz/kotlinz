package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [Ior], rendered as `Left(a)`, `Right(b)` or `Both(a, b)`. */
interface IorShow<A, B>: Show<Ior<A, B>> {
  val showA: Show<A>
  val showB: Show<B>

  override fun show(a: Ior<A, B>): String = when (a) {
    is Ior.Left -> "Left(${showA.show(a.value)})"
    is Ior.Right -> "Right(${showB.show(a.value)})"
    is Ior.Both -> "Both(${showA.show(a.left)}, ${showB.show(a.right)})"
  }
}

/** Returns the [Show] instance for [Ior] derived from the given element instances. */
fun <A, B> Ior.Companion.show(showA: Show<A>, showB: Show<B>): Show<Ior<A, B>> = object: IorShow<A, B> { override val showA = showA; override val showB = showB }
