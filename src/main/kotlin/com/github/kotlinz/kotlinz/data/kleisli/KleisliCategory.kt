package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.type.category.Category

interface KleisliCategory<F> : Category<Kleisli.T, F>, KleisliCompose<F> {
    override fun <A> id(): Kleisli<F, A, A> = Kleisli(monad) { a -> monad.pure(a) }
}