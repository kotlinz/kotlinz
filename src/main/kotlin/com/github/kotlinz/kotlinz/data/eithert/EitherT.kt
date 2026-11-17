package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad

class EitherT<F, E, A>(val monad: Monad<F>, val run: K1<F, Either<E, A>>) : K3<EitherT.T, F, E, A> {
    class T

    companion object {
        fun <F, E, A> narrow(v: K1<K2<T, F, E>, A>): EitherT<F, E, A> = v as EitherT<F, E, A>

        // Monad
        fun <F, E, A> pure(monad: Monad<F>, v: A): EitherT<F, E, A> = instance<F, E>(monad).pure(v)
        fun <F, E, A> join(monad: Monad<F>, v: K1<K2<T, F, E>, K1<K2<T, F, E>, A>>): EitherT<F, E, A> = instance<F, E>(monad).join(v)
        fun <F, E, A> fx(monad: Monad<F>, block: suspend MonadScope<K2<T, F, E>>.() -> A): EitherT<F, E, A> = narrow(instance<F, E>(monad).fx(block))

        // MonadError
        fun <F, E, A> raiseError(monad: Monad<F>, e: E): EitherT<F, E, A> = instance<F, E>(monad).raiseError(e)

        // Transformer
        fun <F, E, A> lift(monad: Monad<F>, v: K1<F, A>): EitherT<F, E, A> = EitherT(monad, monad.fmap({ a -> Either.Right<E, A>(a) }, v))
        fun <F, E, A> fromEither(monad: Monad<F>, v: Either<E, A>): EitherT<F, E, A> = EitherT(monad, monad.pure(v))

        private fun <F, E> instance(monad: Monad<F>) = object: EitherTMonadError<F, E> { override val monad = monad }
    }

    // Monad
    infix fun <B> fmap(f: (A) -> B): EitherT<F, E, B> = instance().fmap(f, this)
    infix fun <B> ap(f: EitherT<F, E, (A) -> B>): EitherT<F, E, B> = instance().ap(f, this)
    infix fun <B> bind(f: (A) -> EitherT<F, E, B>): EitherT<F, E, B> = instance().bind(f, this)

    // MonadError
    infix fun handleErrorWith(f: (E) -> EitherT<F, E, A>): EitherT<F, E, A> = instance().handleErrorWith(f, this)
    infix fun handleError(f: (E) -> A): EitherT<F, E, A> = narrow(instance().handleError(f, this))

    private fun instance() = object: EitherTMonadError<F, E> { override val monad = this@EitherT.monad }
}
