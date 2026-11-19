package com.github.kotlinz.kotlinz.data.cokleisli

import com.github.kotlinz.kotlinz.type.category.Category

/** Category instance for [Cokleisli]; [id] is `extract`. */
interface CokleisliCategory<W> : Category<Cokleisli.T, W>, CokleisliCompose<W> {
    override fun <A> id(): Cokleisli<W, A, A> = Cokleisli(comonad) { wa -> comonad.extract(wa) }
}
