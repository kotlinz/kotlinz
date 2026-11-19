package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.data.forget.ForgetTraversing
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf

/** The monoid keeping the first `Just`, used to extract an optional focus with `Forget`. */
internal fun <A> firstJust(): MonoidOf<Maybe<A>> = MonoidOf(Maybe.None()) { a, b -> if (a is Maybe.Just) a else b }

/** `Forget` instance that returns the first focus, if any. */
internal fun <A> firstForget() = object : ForgetTraversing<Maybe<A>> { override val monoid = firstJust<A>() }
