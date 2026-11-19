package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Knot
import com.github.kotlinz.kotlinz.type.monad.MonadFix

/** MonadFix instance for [Reader]: ties the knot separately for every environment. */
interface ReaderMonadFix<R> : MonadFix<K1<Reader.T, R>>, ReaderMonad<R> {
    override fun <A> mfix(f: (() -> A) -> K1<K1<Reader.T, R>, A>): Reader<R, A> =
        Reader { r -> Knot.fix { x -> Reader.narrow(f(x)).run(r) } }
}
