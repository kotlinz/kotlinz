package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.data.fix.ana as anaScheme
import com.github.kotlinz.kotlinz.data.fix.apo as apoScheme
import com.github.kotlinz.kotlinz.data.fix.cata as cataScheme
import com.github.kotlinz.kotlinz.data.fix.para as paraScheme

data class Fix<F>(val unfix: K1<F, Fix<F>>) {
  companion object {
    fun <F, A> ana(functor: Functor<F>, coalgebra: Coalgebra<F, A>, seed: A): Fix<F> = anaScheme(functor, coalgebra)(seed)
    fun <F, A> apo(functor: Functor<F>, coalgebra: RCoalgebra<F, A>, seed: A): Fix<F> = apoScheme(functor, coalgebra)(seed)
  }

  fun <A> cata(functor: Functor<F>, algebra: Algebra<F, A>): A = cataScheme(functor, algebra)(this)
  fun <A> para(functor: Functor<F>, algebra: RAlgebra<F, A>): A = paraScheme(functor, algebra)(this)
}
