package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either

interface MonadRec<T>: Monad<T> {
  fun <A, B> tailRecM(a: A, f: (A) -> K1<T, Either<A, B>>): K1<T, B>
}
