package com.github.kotlinz.kotlinz.data.free

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadRec
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

@Suppress("UNCHECKED_CAST")
/** Interpreter backing [Free.foldMap]. */
internal object FreeInterpreter {
  // Stack-safe when the target is a MonadRec; otherwise safety depends on the target's bind
  fun <F, M, A> foldMap(free: Free<F, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> =
    if (monad is MonadRec<M>) foldMapRec(free, nt, monad) else foldMapBind(free, nt, monad)

  private fun <F, M, A> foldMapRec(free: Free<F, A>, nt: NaturalTransformation<F, M>, monad: MonadRec<M>): K1<M, A> =
    monad.tailRecM(free) { current -> resume(step(current), nt, monad) }

  private fun <F, M, A> resume(free: Free<F, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, Either<Free<F, A>, A>> =
    when (free) {
      is Free.Pure -> monad.pure(Either.Right(free.value))
      is Free.Suspend -> monad.fmap({ a: A -> Either.Right<Free<F, A>, A>(a) }, nt(free.fa))
      is Free.FlatMap<F, *, A> -> resumeFlatMap(free as Free.FlatMap<F, Any?, A>, nt, monad)
    }

  private fun <F, M, X, A> resumeFlatMap(free: Free.FlatMap<F, X, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, Either<Free<F, A>, A>> =
    monad.fmap({ x: X -> Either.Left<Free<F, A>, A>(free.f(x)) }, nt((free.sub as Free.Suspend<F, X>).fa))

  private fun <F, M, A> foldMapBind(free: Free<F, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> =
    when (val current = step(free)) {
      is Free.Pure -> monad.pure(current.value)
      is Free.Suspend -> nt(current.fa)
      is Free.FlatMap<F, *, A> -> bindFlatMap(current as Free.FlatMap<F, Any?, A>, nt, monad)
    }

  private fun <F, M, X, A> bindFlatMap(free: Free.FlatMap<F, X, A>, nt: NaturalTransformation<F, M>, monad: Monad<M>): K1<M, A> =
    monad.bind({ x: X -> foldMapBind(free.f(x), nt, monad) }, nt((free.sub as Free.Suspend<F, X>).fa))

  // Normalizes into Pure, Suspend or FlatMap over Suspend without recursion
  fun <F, A> step(free: Free<F, A>): Free<F, A> = generateSequence(free) { reassociate(it) }.last()

  private fun <F, A> reassociate(free: Free<F, A>): Free<F, A>? =
    if (free is Free.FlatMap<F, *, A>) reassociateFlatMap(free as Free.FlatMap<F, Any?, A>) else null

  private fun <F, X, A> reassociateFlatMap(free: Free.FlatMap<F, X, A>): Free<F, A>? =
    when (val sub = free.sub) {
      is Free.Pure -> free.f(sub.value)
      is Free.Suspend -> null
      is Free.FlatMap<F, *, X> -> reassociateNested(sub as Free.FlatMap<F, Any?, X>, free.f)
    }

  private fun <F, Y, X, A> reassociateNested(sub: Free.FlatMap<F, Y, X>, f: (X) -> Free<F, A>): Free<F, A> =
    Free.FlatMap(sub.sub) { y -> Free.FlatMap(sub.f(y), f) }
}
