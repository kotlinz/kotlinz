package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.data.lazylist.LazyList.Cell

/** Lazily applies [f] to every element. */
internal fun <A, B> LazyList<A>.mapLazy(f: (A) -> B): LazyList<B> = LazyList.of {
  when (val s = step) {
    is Cell.Cons -> Cell.Cons(f(s.head), s.tail.mapLazy(f))
    is Cell.Nil -> Cell.Nil()
  }
}

/** Lazily appends [other] after this list. */
internal fun <A> LazyList<A>.concat(other: LazyList<A>): LazyList<A> = LazyList.of {
  when (val s = step) {
    is Cell.Cons -> Cell.Cons(s.head, s.tail.concat(other))
    is Cell.Nil -> other.step
  }
}

/** Lazily maps every element to a list and concatenates the results, skipping empty results without recursion. */
internal fun <A, B> LazyList<A>.concatMap(f: (A) -> LazyList<B>): LazyList<B> = LazyList.of {
  cells().mapNotNull { c -> (f(c.head).step as? Cell.Cons)?.let { Cell.Cons(it.head, it.tail.concat(c.tail.concatMap(f))) } }
    .firstOrNull() ?: Cell.Nil()
}

/** The non-empty cells of this list, in order. */
internal fun <A> LazyList<A>.cells(): Sequence<Cell.Cons<A>> =
  generateSequence(step as? Cell.Cons) { it.tail.step as? Cell.Cons }
