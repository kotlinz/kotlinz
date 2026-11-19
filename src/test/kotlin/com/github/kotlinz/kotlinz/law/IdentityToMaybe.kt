package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

object IdentityToMaybe: NaturalTransformation<Identity.T, Maybe.T> {
    override fun <A> invoke(fa: K1<Identity.T, A>): Maybe<A> = Maybe.Just(Identity.narrow(fa).value)
}
