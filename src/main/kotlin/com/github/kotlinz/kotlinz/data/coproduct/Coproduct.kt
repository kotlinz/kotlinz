package com.github.kotlinz.kotlinz.data.coproduct

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.Foldable
import com.github.kotlinz.kotlinz.type.Functor

data class Coproduct<F, G, A>(val run: Either<K1<F, A>, K1<G, A>>): K3<Coproduct.T, F, G, A> {
  class T

  companion object {
    fun <F, G, A> narrow(v: K1<K2<T, F, G>, A>): Coproduct<F, G, A> = v as Coproduct<F, G, A>

    fun <F, G, A> left(v: K1<F, A>): Coproduct<F, G, A> = Coproduct(Either.Left(v))
    fun <F, G, A> right(v: K1<G, A>): Coproduct<F, G, A> = Coproduct(Either.Right(v))

    // Instances
    fun <F, G> functor(functorF: Functor<F>, functorG: Functor<G>): CoproductFunctor<F, G> =
      object: CoproductFunctor<F, G> { override val functorF = functorF; override val functorG = functorG }
    fun <F, G> foldable(foldableF: Foldable<F>, foldableG: Foldable<G>): CoproductFoldable<F, G> =
      object: CoproductFoldable<F, G> { override val foldableF = foldableF; override val foldableG = foldableG }
  }

  fun <B> fold(f: (K1<F, A>) -> B, g: (K1<G, A>) -> B): B = when (run) {
    is Either.Left -> f(run.value)
    is Either.Right -> g(run.value)
  }
}
