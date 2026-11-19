package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A rose tree: a [root] value with an ordered list of [children] subtrees.
 *
 * Folds and traversals visit the nodes in pre-order (a node before its children, children left to
 * right). As a comonad, [duplicate] replaces every node with the subtree rooted at it.
 * Operations recurse once per tree level, so extremely deep trees may exhaust the stack.
 *
 * Instances: Functor, Applicative, Monad, Foldable, Traversable, Copointed, Comonad, Semialign, Zip.
 *
 * ```kotlin
 * val t = tree(1, tree(2, Tree.leaf(4)), Tree.leaf(3))
 * t.flatten()                                    // [1, 2, 4, 3]
 * t.levels()                                     // [[1], [2, 3], [4]]
 * t.extend { Tree.narrow(it).flatten().sum() }   // tree(10, tree(6, Tree.leaf(4)), Tree.leaf(3))
 * Tree.unfoldTree(1) { n -> Pair(n, if (n < 3) listOf(n * 2, n * 2 + 1) else listOf()) }
 * ```
 */
data class Tree<A>(val root: A, val children: List<Tree<A>> = listOf()): K1<Tree.T, A> {
  /** Witness type used to encode `Tree` as a higher-kinded `K1<Tree.T, A>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded `K1<Tree.T, A>` back to [Tree]. */
    fun <A> narrow(v: K1<T, A>): Tree<A> = v as Tree<A>

    /** A tree with no children. */
    fun <A> leaf(value: A): Tree<A> = Tree(value)
    /** Builds a tree from [seed]; [f] returns the node value and the seeds of its children. */
    fun <S, A> unfoldTree(seed: S, f: (S) -> Pair<A, List<S>>): Tree<A> =
      f(seed).let { (value, seeds) -> Tree(value, seeds.map { unfoldTree(it, f) }) }

    // Monad
    /** Creates a leaf. */
    fun <A> pure(v: A): Tree<A> = monad.pure(v)
    /** Flattens a tree of trees, grafting each inner tree in place of its node. */
    fun <A> join(v: K1<T, K1<T, A>>): Tree<A> = monad.join(v)
    /** Runs a monad comprehension; the block is replayed for every node of every bound tree. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Tree<A> = narrow(monad.fxReplay(block))

    // Traversable
    /** Turns a tree of `G` effects into a `G` effect of a tree. */
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)

    private val monad = object: TreeMonad {}
    private val traversable = object: TreeTraversable {}
    private val comonad = object: TreeComonad {}
    private val zip = object: TreeZip {}
  }

  /** The node values in pre-order. */
  fun flatten(): List<A> = buildList { collect(this) }
  /** The node values grouped by depth, starting with the root. */
  fun levels(): List<List<A>> = generateSequence(listOf(this)) { level -> level.flatMap { it.children }.ifEmpty { null } }
    .map { level -> level.map { it.root } }
    .toList()
  /** The number of nodes. */
  fun size(): Int = 1 + children.sumOf { it.size() }

  // Monad
  /** Applies [f] to every node. */
  infix fun <B> fmap(f: (A) -> B): Tree<B> = monad.fmap(f, this)
  /** Applies the functions in [f] to this tree, following the standard rose tree applicative. */
  infix fun <B> ap(f: K1<T, (A) -> B>): Tree<B> = monad.ap(f, this)
  /** Replaces every node with the tree produced by [f], appending the original children after the new ones. */
  infix fun <B> bind(f: (A) -> K1<T, B>): Tree<B> = monad.bind(f, this)

  // Foldable
  /** Folds the nodes in pre-order from the left. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = traversable.foldl(initial, f, this)
  /** Folds the nodes in pre-order from the right. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = traversable.foldr(initial, f, this)
  /** Maps each node into the Monoid [M] and combines the results in pre-order. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = traversable.foldMap(type, f, this)

  // Traversable
  /** Applies an effectful [f] to each node in pre-order and collects the results inside [applicative]. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)

  // Comonad
  /** Returns the [root]. */
  fun extract(): A = comonad.extract(this)
  /** Replaces every node with the subtree rooted at it. */
  fun duplicate(): Tree<K1<T, A>> = comonad.duplicate(this)
  /** Applies [f] to the subtree rooted at every node. */
  fun <B> extend(f: (K1<T, A>) -> B): Tree<B> = comonad.extend(f, this)

  // Semialign / Zip
  /** Pairs nodes at the same position, keeping the subtrees present in only one tree. */
  infix fun <B> align(m: K1<T, B>): Tree<Ior<A, B>> = zip.align(this, m)
  /** Pairs nodes at the same position, dropping the subtrees present in only one tree. */
  infix fun <B> zip(m: K1<T, B>): Tree<Pair<A, B>> = zip.zip(this, m)
  /** Combines nodes at the same position with [f], dropping the subtrees present in only one tree. */
  fun <B, C> zipWith(m: K1<T, B>, f: (A, B) -> C): Tree<C> = zip.zipWith(f, this, m)

  private fun collect(out: MutableList<A>) {
    out.add(root)
    children.forEach { it.collect(out) }
  }
}

/** Creates a [Tree] from a root value and its children. */
fun <A> tree(root: A, vararg children: Tree<A>): Tree<A> = Tree(root, children.toList())
