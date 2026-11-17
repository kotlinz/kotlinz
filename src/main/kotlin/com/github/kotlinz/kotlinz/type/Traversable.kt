package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface Traversable<T>: Functor<T>, Foldable<T> {
  fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<T, A>): K1<G, K1<T, B>>

  fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> =
    traverse(applicative, { ga: K1<G, A> -> ga }, v)
}
