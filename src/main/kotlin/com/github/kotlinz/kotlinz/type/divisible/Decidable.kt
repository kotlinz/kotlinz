package com.github.kotlinz.kotlinz.type.divisible

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either

interface Decidable<T>: Divisible<T> {
  fun <A, B, C> choose(f: (A) -> Either<B, C>, fb: K1<T, B>, fc: K1<T, C>): K1<T, A>
  fun <A> lose(f: (A) -> Nothing): K1<T, A>
}
