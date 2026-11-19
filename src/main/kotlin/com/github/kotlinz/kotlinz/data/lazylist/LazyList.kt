package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A lazy, possibly infinite list whose cells are evaluated on demand and memoized.
 *
 * Each cell is computed at most once, the first time it is inspected, so transformations such as
 * [fmap], [bind] and [take] can be applied to infinite lists. Operations that must visit every
 * element ([foldl], [traverse], [toList], [equals], [hashCode]) only terminate on finite lists.
 *
 * Instances: Functor, Applicative, Monad, Alternative, Foldable, Traversable (finite lists),
 * Semialign, Align, Zip.
 *
 * ```kotlin
 * val naturals = LazyList.iterate(0) { it + 1 }
 * (naturals fmap { it * it }).take(4).toList() // [0, 1, 4, 9]
 * LazyList.fx { lazyListOf(1, 2).bind() * 10 } // LazyList(10, 20)
 * ```
 */
class LazyList<A> private constructor(private val cell: Lazy<Cell<A>>): K1<LazyList.T, A> {
  /** Witness type used to encode `LazyList` as a higher-kinded `K1<LazyList.T, A>`. */
  class T

  internal sealed class Cell<A> {
    class Nil<A>: Cell<A>()
    class Cons<A>(val head: A, val tail: LazyList<A>): Cell<A>()
  }

  internal val step: Cell<A> get() = cell.value

  companion object {
    /** Downcasts a higher-kinded `K1<LazyList.T, A>` back to [LazyList]. */
    fun <A> narrow(v: K1<T, A>): LazyList<A> = v as LazyList<A>

    internal fun <A> of(step: () -> Cell<A>): LazyList<A> = LazyList(lazy(step))

    /** The empty list. */
    fun <A> empty(): LazyList<A> = of { Cell.Nil() }
    /** A list with [head] followed by the lazily computed [tail]. */
    fun <A> cons(head: A, tail: () -> LazyList<A>): LazyList<A> = of { Cell.Cons(head, defer(tail)) }
    /** Defers building a list until it is first inspected. */
    fun <A> defer(f: () -> LazyList<A>): LazyList<A> = of { f().step }
    /** A finite list holding [values]. */
    fun <A> fromList(values: List<A>): LazyList<A> = values.foldRight(empty()) { a, acc -> of { Cell.Cons(a, acc) } }
    /** The infinite list `seed, f(seed), f(f(seed)), ...`. */
    fun <A> iterate(seed: A, f: (A) -> A): LazyList<A> = cons(seed) { iterate(f(seed), f) }
    /** The infinite list repeating [value]. */
    fun <A> repeat(value: A): LazyList<A> = iterate(value) { it }
    /** Builds a list from [seed], stopping when [f] returns [Maybe.None]. */
    fun <S, A> unfold(seed: S, f: (S) -> Maybe<Pair<A, S>>): LazyList<A> = of {
      when (val next = f(seed)) {
        is Maybe.Just -> Cell.Cons(next.value.first, unfold(next.value.second, f))
        is Maybe.None -> Cell.Nil()
      }
    }

    // Monad
    /** Creates a singleton list. */
    fun <A> pure(v: A): LazyList<A> = monad.pure(v)
    /** Flattens a list of lists lazily. */
    fun <A> join(v: K1<T, K1<T, A>>): LazyList<A> = monad.join(v)
    /** Runs a monad comprehension; the block is replayed for every bound element (non-deterministic choice). */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): LazyList<A> = narrow(monad.fxReplay(block))

    // Traversable
    /** Turns a finite list of `G` effects into a `G` effect of a list. */
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)

    // Align
    /** Returns the empty list, the identity of [align]. */
    fun <A> nil(): LazyList<A> = zip.nil()

    private val monad = object: LazyListMonad {}
    private val alternative = object: LazyListAlternative {}
    private val traversable = object: LazyListTraversable {}
    private val zip = object: LazyListZip, LazyListAlign {}
  }

  /** Returns the first element and the rest, or [Maybe.None] when empty. */
  fun uncons(): Maybe<Pair<A, LazyList<A>>> = when (val s = step) {
    is Cell.Cons -> Maybe.Just(Pair(s.head, s.tail))
    is Cell.Nil -> Maybe.None()
  }
  /** Returns the first element, or [Maybe.None] when empty. */
  fun head(): Maybe<A> = uncons() fmap { it.first }
  /** Returns true when the list has no elements; forces only the first cell. */
  fun isEmpty(): Boolean = step is Cell.Nil
  /** Lazily concatenates [other] after this list. */
  operator fun plus(other: LazyList<A>): LazyList<A> = alternative.orElse(this, other)
  /** The first [n] elements. */
  fun take(n: Int): LazyList<A> = of { if (n <= 0) Cell.Nil() else consOrNil { Cell.Cons(it.head, it.tail.take(n - 1)) } }
  /** The list without its first [n] elements. */
  fun drop(n: Int): LazyList<A> = of { dropStep(n) }
  /** The longest prefix whose elements satisfy [p]. */
  fun takeWhile(p: (A) -> Boolean): LazyList<A> = of { consOrNil { if (p(it.head)) Cell.Cons(it.head, it.tail.takeWhile(p)) else Cell.Nil() } }
  /** Forces every element into a [List]; only terminates for finite lists. */
  fun toList(): List<A> = asSequence().toList()
  /** Views this list as a Kotlin [Sequence] that forces cells as it is iterated. */
  fun asSequence(): Sequence<A> = Sequence { cells().map { it.head }.iterator() }

  // Monad
  /** Applies [f] to every element lazily. */
  infix fun <B> fmap(f: (A) -> B): LazyList<B> = monad.fmap(f, this)
  /** Applies every function in [f] to every element. */
  infix fun <B> ap(f: K1<T, (A) -> B>): LazyList<B> = monad.ap(f, this)
  /** Maps each element to a list and concatenates the results lazily. */
  infix fun <B> bind(f: (A) -> K1<T, B>): LazyList<B> = monad.bind(f, this)

  // Alternative
  /** Lazily concatenates this list with [m]. */
  infix fun orElse(m: K1<T, A>): LazyList<A> = alternative.orElse(this, m)

  // Foldable
  /** Folds the elements from the left; only terminates for finite lists. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = traversable.foldl(initial, f, this)
  /** Folds the elements from the right; only terminates for finite lists. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = traversable.foldr(initial, f, this)
  /** Maps each element into the Monoid [M] and combines the results. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = traversable.foldMap(type, f, this)

  // Traversable
  /** Applies an effectful [f] to each element of a finite list and collects the results inside [applicative]. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)

  // Semialign / Zip
  /** Pairs this list with [m] by index lazily, keeping the leftover elements of the longer one. */
  infix fun <B> align(m: K1<T, B>): LazyList<Ior<A, B>> = zip.align(this, m)
  /** Pairs this list with [m] by index lazily, truncating to the shorter one. */
  infix fun <B> zip(m: K1<T, B>): LazyList<Pair<A, B>> = zip.zip(this, m)
  /** Combines this list with [m] by index lazily using [f], truncating to the shorter one. */
  fun <B, C> zipWith(m: K1<T, B>, f: (A, B) -> C): LazyList<C> = zip.zipWith(f, this, m)

  /** Compares element by element; only terminates when at least one list is finite. */
  override fun equals(other: Any?): Boolean = other is LazyList<*> && sameElements(this, other)
  override fun hashCode(): Int = toList().hashCode()
  /** Shows only the cells evaluated so far, ending with `?` when the rest is not computed yet. */
  override fun toString(): String = "LazyList(" + evaluatedPrefix().joinToString(", ") + ")"

  private fun consOrNil(f: (Cell.Cons<A>) -> Cell<A>): Cell<A> = when (val s = step) {
    is Cell.Cons -> f(s)
    is Cell.Nil -> s
  }

  private fun dropStep(n: Int): Cell<A> = generateSequence(Pair(this, n)) { (list, k) ->
    if (k > 0) (list.step as? Cell.Cons)?.let { Pair(it.tail, k - 1) } else null
  }.last().first.step

  private fun evaluatedPrefix(): List<String> = generateSequence(this) { it.evaluatedCons()?.tail }
    .mapNotNull { list -> if (!list.cell.isInitialized()) "?" else list.evaluatedCons()?.let { it.head.toString() } }
    .toList()

  private fun evaluatedCons(): Cell.Cons<A>? = if (cell.isInitialized()) cell.value as? Cell.Cons else null
}

/** Creates a finite [LazyList] from the given values. */
fun <A> lazyListOf(vararg values: A): LazyList<A> = LazyList.fromList(values.toList())
/** Converts this [List] to a [LazyList]. */
fun <A> List<A>.toLazyList(): LazyList<A> = LazyList.fromList(this)

private fun sameElements(a: LazyList<*>, b: LazyList<*>): Boolean =
  generateSequence(Pair(a, b)) { (x, y) ->
    val sx = x.step
    val sy = y.step
    if (sx is LazyList.Cell.Cons && sy is LazyList.Cell.Cons && sx.head == sy.head) Pair(sx.tail, sy.tail) else null
  }.last().let { (x, y) -> x.step is LazyList.Cell.Nil && y.step is LazyList.Cell.Nil }
