package com.github.kotlinz.kotlinz.data.fold

import com.github.kotlinz.kotlinz.data.maybe.Maybe

/** Sums the inputs. */
fun Fold.Companion.sum(): Fold<Int, Int> = of(0, { s, a: Int -> s + a }) { it }

/** Multiplies the inputs. */
fun Fold.Companion.product(): Fold<Int, Int> = of(1, { s, a: Int -> s * a }) { it }

/** Counts the inputs. */
fun <A> Fold.Companion.length(): Fold<A, Int> = of(0, { s, _: A -> s + 1 }) { it }

/** Returns the largest input, or `None` when there are no inputs. */
fun <A: Comparable<A>> Fold.Companion.maximum(): Fold<A, Maybe<A>> = best { a, b -> a >= b }

/** Returns the smallest input, or `None` when there are no inputs. */
fun <A: Comparable<A>> Fold.Companion.minimum(): Fold<A, Maybe<A>> = best { a, b -> a <= b }

/** Collects the inputs into a list, preserving their order. */
fun <A> Fold.Companion.list(): Fold<A, List<A>> =
  of<Node<A>?, A, List<A>>(null, { s, a -> Node(a, s) }) { s -> generateSequence(s) { it.tail }.map { it.head }.toList().asReversed() }

/** Returns whether any input satisfies [p]. */
fun <A> Fold.Companion.any(p: (A) -> Boolean): Fold<A, Boolean> = of(false, { s, a: A -> s || p(a) }) { it }

/** Returns whether every input satisfies [p]. */
fun <A> Fold.Companion.all(p: (A) -> Boolean): Fold<A, Boolean> = of(true, { s, a: A -> s && p(a) }) { it }

private fun <A> Fold.Companion.best(keep: (A, A) -> Boolean): Fold<A, Maybe<A>> =
  of<Maybe<A>, A, Maybe<A>>(Maybe.None(), { s, a -> if (s is Maybe.Just && keep(s.value, a)) s else Maybe.Just(a) }) { it }

private class Node<A>(val head: A, val tail: Node<A>?)
