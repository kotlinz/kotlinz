package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

/** MonadWriter instance for [EitherT], lifted from a base monad that has MonadWriter. */
interface EitherTMonadWriter<F, E, W> : MonadWriter<K2<EitherT.T, F, E>, W>, EitherTMonadTrans<F, E> {
    /** The underlying monad with output `W`. */
    override val monad: MonadWriter<F, W>

    override fun <A> writer(v: Pair<A, W>): EitherT<F, E, A> = lift(monad.writer(v))

    override fun <A> listen(v: K1<K2<EitherT.T, F, E>, A>): EitherT<F, E, Pair<A, W>> {
        val run = monad.fmap({ (e, w): Pair<Either<E, A>, W> -> e fmap { a -> Pair(a, w) } }, monad.listen(EitherT.narrow(v).run))
        return EitherT(monad, run)
    }

    override fun <A> pass(v: K1<K2<EitherT.T, F, E>, Pair<A, (W) -> W>>): EitherT<F, E, A> {
        return EitherT(monad, monad.pass(monad.fmap({ e: Either<E, Pair<A, (W) -> W>> -> passing(e) }, EitherT.narrow(v).run)))
    }

    private fun <A> passing(e: Either<E, Pair<A, (W) -> W>>): Pair<Either<E, A>, (W) -> W> = when (e) {
        is Either.Right -> Pair(Either.Right(e.value.first), e.value.second)
        is Either.Left -> Pair(Either.Left(e.value), { w -> w })
    }
}
