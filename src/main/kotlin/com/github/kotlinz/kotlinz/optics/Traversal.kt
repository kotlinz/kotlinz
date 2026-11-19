package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.forget.Forget
import com.github.kotlinz.kotlinz.data.forget.ForgetTraversing
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Traversing
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.data.star.Star
import com.github.kotlinz.kotlinz.data.star.StarTraversing
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.profunctor.Traversing
import com.github.kotlinz.kotlinz.type.profunctor.Wander

/**
 * A focus on zero or more `A`s inside an `S`, visited in order.
 *
 * Profunctor encoding: a `Traversal` lifts any [Traversing] profunctor value `P<A, B>` to `P<S, T>`
 * via the generic method [runTraversing]. Every [Optional] (hence every Lens, Prism and Iso) is a
 * `Traversal`. Ready-made traversals for `List`, `ListK`, `NonEmptyList` and `Maybe` live on the companion.
 *
 * Laws:
 * - Identity: `traverseOf(Identity, ::Identity, s) == Identity(s)` (equivalently `modify { it }(s) == s`)
 * - Composition: `modify(f)(modify(g)(s)) == modify { f(g(it)) }(s)`, and traversing with a composed
 *   applicative equals traversing twice
 *
 * ```kotlin
 * val each = Traversal.list<Int, Int>()
 * each.getAll(listOf(1, 2))                    // [1, 2]
 * each.modify { it * 10 }(listOf(1, 2))        // [10, 20]
 * each.traverseOf(object : MaybeApplicative {}, { n: Int -> Maybe.Just(n) }, listOf(1)) // Just([1])
 * ```
 */
interface Traversal<S, T, A, B> {
  /** Lifts [pab] through this traversal using [traversing]. */
  fun <P, F> runTraversing(traversing: Traversing<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T>

  /** Hosts ready-made traversals such as [list] and [maybe]. */
  companion object
}

/** Builds a [Traversal] from a van Laarhoven traversal. */
fun <S, T, A, B> Traversal(wander: Wander<S, T, A, B>): Traversal<S, T, A, B> = object : Traversal<S, T, A, B> {
  override fun <P, F> runTraversing(traversing: Traversing<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T> =
    traversing.wander(wander, pab)
}

/** Runs the effectful [f] on every focus, collecting the effects of [applicative]. */
fun <S, T, A, B, G> Traversal<S, T, A, B>.traverseOf(applicative: Applicative<G>, f: (A) -> K1<G, B>, s: S): K1<G, T> =
  Star.narrow(runTraversing(object : StarTraversing<G> { override val applicative = applicative }, Star(f))).run(s)

/** Collects every focus in order. */
fun <S, T, A, B> Traversal<S, T, A, B>.getAll(s: S): List<A> {
  val list = object : ForgetTraversing<List<A>> { override val monoid = MonoidOf(listOf<A>()) { a, b -> a + b } }
  return Forget.narrow(runTraversing(list, Forget<List<A>, A, B> { listOf(it) })).run(s)
}

/** Replaces every focus with [b]. */
fun <S, T, A, B> Traversal<S, T, A, B>.set(b: B): (S) -> T = modify { b }

/** Modifies every focus with [f]. */
fun <S, T, A, B> Traversal<S, T, A, B>.modify(f: (A) -> B): (S) -> T =
  Function1.narrow(runTraversing(object : Function1Traversing {}, Function1(f))).run

/** Composes with [other], focusing first through this traversal and then through [other]. */
infix fun <S, T, A, B, C, D> Traversal<S, T, A, B>.andThen(other: Traversal<A, B, C, D>): Traversal<S, T, C, D> =
  object : Traversal<S, T, C, D> {
    override fun <P, F> runTraversing(traversing: Traversing<P, F>, pab: K3<P, F, C, D>): K3<P, F, S, T> =
      this@andThen.runTraversing(traversing, other.runTraversing(traversing, pab))
  }
