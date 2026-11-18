package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.data.fix.ana as anaScheme
import com.github.kotlinz.kotlinz.data.fix.apo as apoScheme
import com.github.kotlinz.kotlinz.data.fix.cata as cataScheme
import com.github.kotlinz.kotlinz.data.fix.para as paraScheme

/**
 * The fixed point of a functor `F`: a recursive structure built from a non-recursive pattern functor.
 *
 * Use it with recursion schemes ([cata], [ana], [para], [apo], [hylo]) to separate the recursion from the
 * per-layer logic. Recursion depth is bounded by the JVM call stack.
 *
 * ```kotlin
 * val natFunctor = object : NatFFunctor {} // user-defined pattern functor
 * val toNat: Coalgebra<NatF.T, Int> = { n -> if (n <= 0) NatF.Zero() else NatF.Succ(n - 1) }
 * val toInt: Algebra<NatF.T, Int> = { v -> when (val nat = NatF.narrow(v)) { is NatF.Zero -> 0; is NatF.Succ -> nat.prev + 1 } }
 * Fix.ana(natFunctor, toNat, 3).cata(natFunctor, toInt) // 3
 * ```
 *
 * @property unfix the outermost layer, whose children are again [Fix] values.
 */
data class Fix<F>(val unfix: K1<F, Fix<F>>) {
  companion object {
    /** Builds a structure by unfolding [seed] with [coalgebra] (anamorphism). */
    fun <F, A> ana(functor: Functor<F>, coalgebra: Coalgebra<F, A>, seed: A): Fix<F> = anaScheme(functor, coalgebra)(seed)
    /** Builds a structure from [seed] with [coalgebra], which may stop early (apomorphism). */
    fun <F, A> apo(functor: Functor<F>, coalgebra: RCoalgebra<F, A>, seed: A): Fix<F> = apoScheme(functor, coalgebra)(seed)
  }

  /** Folds this structure bottom-up with [algebra] (catamorphism). */
  fun <A> cata(functor: Functor<F>, algebra: Algebra<F, A>): A = cataScheme(functor, algebra)(this)
  /** Folds this structure with [algebra], which also sees each original subterm (paramorphism). */
  fun <A> para(functor: Functor<F>, algebra: RAlgebra<F, A>): A = paraScheme(functor, algebra)(this)
}
