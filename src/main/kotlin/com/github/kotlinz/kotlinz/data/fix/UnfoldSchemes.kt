package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.free.Free
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * Futumorphism: like [ana], but [coalgebra] may emit several layers at once. Each child is a [Free]
 * program whose `liftF` layers are emitted as-is and whose `pure` leaves are seeds to keep unfolding.
 */
fun <F, A> futu(functor: Functor<F>, coalgebra: CVCoalgebra<F, A>): (A) -> Fix<F> =
  { seed -> Fix(functor.fmap({ free: Free<F, A> -> unfoldFree(functor, coalgebra, free) }, coalgebra(seed))) }

/** Postpromorphism: like [ana], but [nt] is applied to every layer below the current one after unfolding it. */
fun <F, A> postpro(functor: Functor<F>, nt: NaturalTransformation<F, F>, coalgebra: Coalgebra<F, A>): (A) -> Fix<F> =
  { seed -> Fix(functor.fmap({ next: A -> transformAll(functor, nt, postpro(functor, nt, coalgebra)(next)) }, coalgebra(seed))) }

/**
 * Elgot coalgebra: a [hylo] whose [algebra] sees the current seed and the folded layer below it.
 * The layer is passed lazily, so the algebra may finish without unfolding further.
 */
fun <F, A, B> coelgot(functor: Functor<F>, algebra: (A, () -> K1<F, B>) -> B, coalgebra: Coalgebra<F, A>): (A) -> B =
  { seed -> algebra(seed) { functor.fmap(coelgot(functor, algebra, coalgebra), coalgebra(seed)) } }

private fun <F, A> unfoldFree(functor: Functor<F>, coalgebra: CVCoalgebra<F, A>, free: Free<F, A>): Fix<F> =
  when (val step = resume(functor, free)) {
    is Either.Left -> futu(functor, coalgebra)(step.value)
    is Either.Right -> Fix(functor.fmap({ next: Free<F, A> -> unfoldFree(functor, coalgebra, next) }, step.value))
  }

private fun <F> transformAll(functor: Functor<F>, nt: NaturalTransformation<F, F>, fix: Fix<F>): Fix<F> =
  ana(functor) { f: Fix<F> -> nt(f.unfix) }(fix)

@Suppress("UNCHECKED_CAST")
private tailrec fun <F, A> resume(functor: Functor<F>, free: Free<F, A>): Either<A, K1<F, Free<F, A>>> = when (free) {
  is Free.Pure -> Either.Left(free.value)
  is Free.Suspend -> Either.Right(functor.fmap({ a: A -> Free.Pure<F, A>(a) }, free.fa))
  is Free.FlatMap<F, *, A> -> {
    val bound = free as Free.FlatMap<F, Any?, A>
    when (val sub = bound.sub) {
      is Free.Pure -> resume(functor, bound.f(sub.value))
      is Free.Suspend -> Either.Right(functor.fmap(bound.f, sub.fa))
      is Free.FlatMap<F, *, Any?> -> {
        val inner = sub as Free.FlatMap<F, Any?, Any?>
        resume(functor, Free.FlatMap(inner.sub) { x -> Free.FlatMap(inner.f(x), bound.f) })
      }
    }
  }
}
