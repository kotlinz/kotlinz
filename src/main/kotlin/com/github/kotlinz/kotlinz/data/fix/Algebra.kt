package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.cofree.Cofree
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.free.Free

/** An F-algebra: collapses one layer of `F` into a result, used by [cata] and [hylo]. */
typealias Algebra<F, A> = (K1<F, A>) -> A
/** An F-coalgebra: expands a seed into one layer of `F`, used by [ana] and [hylo]. */
typealias Coalgebra<F, A> = (A) -> K1<F, A>
/** An R-algebra: like [Algebra] but also sees the original subterm, used by [para]. */
typealias RAlgebra<F, A> = (K1<F, Pair<Fix<F>, A>>) -> A
/** An R-coalgebra: like [Coalgebra] but may stop early with a finished subterm ([Either.Left]), used by [apo]. */
typealias RCoalgebra<F, A> = (A) -> K1<F, Either<Fix<F>, A>>
/** A course-of-values algebra: sees the results for every subterm, recursively, used by [histo]. */
typealias CVAlgebra<F, A> = (K1<F, Cofree<F, A>>) -> A
/** A course-of-values coalgebra: may emit several layers at once as a [Free] structure, used by [futu]. */
typealias CVCoalgebra<F, A> = (A) -> K1<F, Free<F, A>>
