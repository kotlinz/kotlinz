package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [WriterT], moving it from base `F` to base `G`. */
interface WriterTMFunctor<F, G, W: Monoid<*, W>> : MFunctor<F, G, K2<WriterT.T, F, W>, K2<WriterT.T, G, W>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>
    /** The Monoid class of the log. */
    val type: Class<W>

    override fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<K2<WriterT.T, F, W>, A>): WriterT<G, W, A> {
        return WriterT(target, type, nt(WriterT.narrow(v).run))
    }
}
