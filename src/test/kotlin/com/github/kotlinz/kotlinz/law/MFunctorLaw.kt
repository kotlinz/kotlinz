package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.matchers.shouldBe

object MFunctorLaw {
    fun <F, TF, A> identity(instance: MFunctor<F, F, TF, TF>, v: K1<TF, A>, run: (K1<TF, *>) -> Any?) {
        run(instance.hoist(NaturalTransformation.identity(), v)) shouldBe run(v)
    }

    fun <F, G, H, TF, TG, TH, A> composition(
        fg: MFunctor<F, G, TF, TG>,
        gh: MFunctor<G, H, TG, TH>,
        fh: MFunctor<F, H, TF, TH>,
        f: NaturalTransformation<F, G>,
        g: NaturalTransformation<G, H>,
        v: K1<TF, A>,
        run: (K1<TH, *>) -> Any?
    ) {
        run(fh.hoist(f andThen g, v)) shouldBe run(gh.hoist(g, fg.hoist(f, v)))
    }
}
