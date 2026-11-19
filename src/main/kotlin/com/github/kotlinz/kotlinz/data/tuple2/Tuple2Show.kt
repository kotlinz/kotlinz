package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [Tuple2], rendered as `Tuple2(a, b)`. */
interface Tuple2Show<A, B>: Show<Tuple2<A, B>> {
  val showA: Show<A>
  val showB: Show<B>

  override fun show(a: Tuple2<A, B>): String = "Tuple2(${showA.show(a.first)}, ${showB.show(a.second)})"
}

/** Returns the [Show] instance for [Tuple2] derived from the given element instances. */
fun <A, B> Tuple2.Companion.show(showA: Show<A>, showB: Show<B>): Show<Tuple2<A, B>> = object: Tuple2Show<A, B> { override val showA = showA; override val showB = showB }
