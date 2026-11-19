package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.data.maybe.Maybe

/** Internal tree representation of a [Chain]; [Concat] never holds an empty child. */
internal sealed class ChainNode<A>(val size: Int) {
  class Empty<A>: ChainNode<A>(0)
  class Single<A>(val value: A): ChainNode<A>(1)
  class Wrap<A>(val values: List<A>): ChainNode<A>(values.size)
  class Concat<A>(val left: ChainNode<A>, val right: ChainNode<A>): ChainNode<A>(left.size + right.size)

  /** Splits off the first element by walking down the left spine, without recursion. */
  fun uncons(): Maybe<Pair<A, Chain<A>>> {
    val spine = generateSequence(this) { (it as? Concat)?.left }.toList()
    val rights = spine.dropLast(1).map { (it as Concat).right }
    return when (val leaf = spine.last()) {
      is Single -> Maybe.Just(Pair(leaf.value, rebuild(Chain.empty(), rights)))
      is Wrap -> Maybe.Just(Pair(leaf.values.first(), rebuild(Chain.fromList(leaf.values.subList(1, leaf.size)), rights)))
      else -> Maybe.None()
    }
  }

  private fun rebuild(rest: Chain<A>, rights: List<ChainNode<A>>): Chain<A> =
    rights.asReversed().fold(rest) { acc, node -> acc + Chain(node) }
}

/** Iterates a [ChainNode] tree in order using an explicit stack. */
internal class ChainIterator<A>(root: ChainNode<A>): Iterator<A> {
  private val stack = ArrayDeque(listOf(root))
  private var current: Iterator<A> = emptyList<A>().iterator()

  override fun hasNext(): Boolean = advance().hasNext()
  override fun next(): A = advance().next()

  private fun advance(): Iterator<A> {
    while (!current.hasNext() && stack.isNotEmpty()) expand(stack.removeLast())
    return current
  }

  private fun expand(node: ChainNode<A>) {
    when (node) {
      is ChainNode.Empty -> Unit
      is ChainNode.Single -> current = listOf(node.value).iterator()
      is ChainNode.Wrap -> current = node.values.iterator()
      is ChainNode.Concat -> stack.addAll(listOf(node.right, node.left))
    }
  }
}
