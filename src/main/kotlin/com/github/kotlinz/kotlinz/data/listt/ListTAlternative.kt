package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative

/** Alternative instance for [ListT]: [empty] is the empty stream and [orElse] concatenates. */
interface ListTAlternative<F> : Alternative<K1<ListT.T, F>>, ListTApplicative<F> {
    override fun <A> empty(): ListT<F, A> = ListT.empty(monad)

    override fun <A> orElse(v1: K1<K1<ListT.T, F>, A>, v2: K1<K1<ListT.T, F>, A>): ListT<F, A> {
        return ListT.narrow(v1) concat ListT.narrow(v2)
    }
}
