package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.Functor

// Recursion depth is bounded by the JVM call stack: a generic Functor offers no way to
// suspend inside fmap, so every layer of the structure consumes a few stack frames.

/** Catamorphism: folds a [Fix] bottom-up with [algebra]. */
fun <F, A> cata(functor: Functor<F>, algebra: Algebra<F, A>): (Fix<F>) -> A =
  { fix -> algebra(functor.fmap(cata(functor, algebra), fix.unfix)) }

/** Anamorphism: unfolds a seed into a [Fix] with [coalgebra]. */
fun <F, A> ana(functor: Functor<F>, coalgebra: Coalgebra<F, A>): (A) -> Fix<F> =
  { seed -> Fix(functor.fmap(ana(functor, coalgebra), coalgebra(seed))) }

/** Hylomorphism: an unfold followed by a fold, fused so no intermediate [Fix] is built. */
fun <F, A, B> hylo(functor: Functor<F>, algebra: Algebra<F, B>, coalgebra: Coalgebra<F, A>): (A) -> B =
  { seed -> algebra(functor.fmap(hylo(functor, algebra, coalgebra), coalgebra(seed))) }

/** Paramorphism: like [cata], but [algebra] also receives each original subterm. */
fun <F, A> para(functor: Functor<F>, algebra: RAlgebra<F, A>): (Fix<F>) -> A =
  { fix -> algebra(functor.fmap({ child: Fix<F> -> Pair(child, para(functor, algebra)(child)) }, fix.unfix)) }

/** Apomorphism: like [ana], but [coalgebra] may return a finished subterm to stop unfolding. */
fun <F, A> apo(functor: Functor<F>, coalgebra: RCoalgebra<F, A>): (A) -> Fix<F> =
  { seed -> Fix(functor.fmap({ next: Either<Fix<F>, A> -> resumeApo(functor, coalgebra, next) }, coalgebra(seed))) }

private fun <F, A> resumeApo(functor: Functor<F>, coalgebra: RCoalgebra<F, A>, next: Either<Fix<F>, A>): Fix<F> =
  when (next) {
    is Either.Left -> next.value
    is Either.Right -> apo(functor, coalgebra)(next.value)
  }
