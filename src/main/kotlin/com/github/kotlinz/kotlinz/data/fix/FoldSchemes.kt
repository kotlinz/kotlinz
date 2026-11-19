package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.cofree.Cofree
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** Histomorphism: like [cata], but [algebra] sees the results for all subterms as a [Cofree] history. */
fun <F, A> histo(functor: Functor<F>, algebra: CVAlgebra<F, A>): (Fix<F>) -> A =
  { fix -> history(functor, algebra, fix).head }

/** Zygomorphism: like [cata], but [algebra] also sees the result of the auxiliary [helper] fold for each subterm. */
fun <F, A, B> zygo(functor: Functor<F>, helper: Algebra<F, B>, algebra: (K1<F, Pair<B, A>>) -> A): (Fix<F>) -> A =
  { fix -> cata(functor, zygoAlgebra(functor, helper, algebra))(fix).second }

/** Prepromorphism: like [cata], but [nt] is applied to every layer below the current one before folding it. */
fun <F, A> prepro(functor: Functor<F>, nt: NaturalTransformation<F, F>, algebra: Algebra<F, A>): (Fix<F>) -> A =
  { fix -> algebra(functor.fmap({ child: Fix<F> -> prepro(functor, nt, algebra)(transform(functor, nt, child)) }, fix.unfix)) }

/**
 * Elgot algebra: a [hylo] whose [coalgebra] may short-circuit with a finished result ([Either.Left])
 * instead of producing another layer ([Either.Right]).
 */
fun <F, A, B> elgot(functor: Functor<F>, algebra: Algebra<F, B>, coalgebra: (A) -> Either<B, K1<F, A>>): (A) -> B =
  { seed ->
    when (val step = coalgebra(seed)) {
      is Either.Left -> step.value
      is Either.Right -> algebra(functor.fmap(elgot(functor, algebra, coalgebra), step.value))
    }
  }

private fun <F, A> history(functor: Functor<F>, algebra: CVAlgebra<F, A>, fix: Fix<F>): Cofree<F, A> {
  val layer = functor.fmap({ child: Fix<F> -> history(functor, algebra, child) }, fix.unfix)
  return Cofree(algebra(layer)) { layer }
}

private fun <F, A, B> zygoAlgebra(functor: Functor<F>, helper: Algebra<F, B>, algebra: (K1<F, Pair<B, A>>) -> A): Algebra<F, Pair<B, A>> =
  { layer -> Pair(helper(functor.fmap({ p: Pair<B, A> -> p.first }, layer)), algebra(layer)) }

private fun <F> transform(functor: Functor<F>, nt: NaturalTransformation<F, F>, fix: Fix<F>): Fix<F> =
  cata(functor) { layer: K1<F, Fix<F>> -> Fix(nt(layer)) }(fix)
