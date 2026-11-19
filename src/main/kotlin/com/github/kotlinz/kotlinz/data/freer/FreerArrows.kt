package com.github.kotlinz.kotlinz.data.freer

/** A type-erased, catenable queue of [Freer] continuations ("reflection without remorse"). */
internal sealed class FreerArrows<F> {
  class Leaf<F>(val f: (Any?) -> Freer<F, Any?>): FreerArrows<F>()
  class Node<F>(val left: FreerArrows<F>, val right: FreerArrows<F>): FreerArrows<F>()

  infix fun append(other: FreerArrows<F>): FreerArrows<F> = Node(this, other)

  fun applyTo(x: Any?): Freer<F, Any?> = applyLoop(this, x)

  private companion object {
    tailrec fun <F> applyLoop(arrows: FreerArrows<F>, x: Any?): Freer<F, Any?> {
      val (head, rest) = uncons(arrows)
      val result = head(x)
      return when {
        rest == null -> result
        result is Freer.Pure -> applyLoop(rest, result.value)
        else -> (result as Freer.Impure).let { Freer.Impure(it.instruction, it.arrows append rest) }
      }
    }

    tailrec fun <F> uncons(arrows: FreerArrows<F>): Pair<(Any?) -> Freer<F, Any?>, FreerArrows<F>?> = when (arrows) {
      is Leaf -> Pair(arrows.f, null)
      is Node -> when (val left = arrows.left) {
        is Leaf -> Pair(left.f, arrows.right)
        is Node -> uncons(Node(left.left, Node(left.right, arrows.right)))
      }
    }
  }
}
