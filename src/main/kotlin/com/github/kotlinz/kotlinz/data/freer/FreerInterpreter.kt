package com.github.kotlinz.kotlinz.data.freer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadRec
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** Interpreter backing [Freer.foldMap]. */
internal object FreerInterpreter {
  fun <F, M, A> foldMap(freer: Freer<F, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> =
    if (monad is MonadRec<M>) monad.tailRecM(freer) { step(it.view(), nt, monad) } else foldMapBind(freer, nt, monad)

  private fun <F, M, A> step(view: FreerView<F, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, Either<Freer<F, A>, A>> =
    when (view) {
      is FreerView.Return -> monad.pure(Either.Right(view.value))
      is FreerView.Bind<F, *, A> -> stepBind(view, nt, monad)
    }

  private fun <F, M, X, A> stepBind(view: FreerView.Bind<F, X, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, Either<Freer<F, A>, A>> =
    monad.fmap({ x: X -> Either.Left<Freer<F, A>, A>(view.continuation(x)) }, nt(view.instruction))

  private fun <F, M, A> foldMapBind(freer: Freer<F, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> =
    when (val view = freer.view()) {
      is FreerView.Return -> monad.pure(view.value)
      is FreerView.Bind<F, *, A> -> bindStep(view, nt, monad)
    }

  private fun <F, M, X, A> bindStep(view: FreerView.Bind<F, X, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> =
    monad.bind({ x: X -> foldMapBind(view.continuation(x), nt, monad) }, nt(view.instruction))
}
