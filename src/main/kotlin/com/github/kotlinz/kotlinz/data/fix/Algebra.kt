package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either

typealias Algebra<F, A> = (K1<F, A>) -> A
typealias Coalgebra<F, A> = (A) -> K1<F, A>
typealias RAlgebra<F, A> = (K1<F, Pair<Fix<F>, A>>) -> A
typealias RCoalgebra<F, A> = (A) -> K1<F, Either<Fix<F>, A>>
