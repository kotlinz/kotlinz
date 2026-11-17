package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe

interface Alternative<T>: Applicative<T> {
  fun <A> empty(): K1<T, A>
  fun <A> orElse(v1: K1<T, A>, v2: K1<T, A>): K1<T, A>

  fun <A> optional(v: K1<T, A>): K1<T, Maybe<A>> =
    orElse(fmap({ a: A -> Maybe.Just(a) }, v), pure(Maybe.None()))
}
