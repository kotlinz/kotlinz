package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.profunctor.Choice

/** The prism on the value of a [Maybe.Just]. */
fun <A, B> Prism.Companion.just(): Prism<Maybe<A>, Maybe<B>, A, B> = Prism.matching(
  { m: Maybe<A> -> if (m is Maybe.Just) Either.Right(m.value) else Either.Left(Maybe.None()) },
  { b: B -> Maybe.Just(b) }
)

/** The prism on [Maybe.None]. */
fun <A> Prism.Companion.none(): Prism<Maybe<A>, Maybe<A>, Unit, Unit> =
  Prism({ m: Maybe<A> -> if (m is Maybe.None) Maybe.Just(Unit) else Maybe.None() }, { _: Unit -> Maybe.None() })

/** The prism on the value of an [Either.Left]. */
fun <A, B, C> Prism.Companion.left(): Prism<Either<A, C>, Either<B, C>, A, B> = object : Prism<Either<A, C>, Either<B, C>, A, B> {
  override fun <P, F> runChoice(choice: Choice<P, F>, pab: K3<P, F, A, B>) = choice.left<A, B, C>(pab)
}

/** The prism on the value of an [Either.Right]. */
fun <A, B, C> Prism.Companion.right(): Prism<Either<C, A>, Either<C, B>, A, B> = object : Prism<Either<C, A>, Either<C, B>, A, B> {
  override fun <P, F> runChoice(choice: Choice<P, F>, pab: K3<P, F, A, B>) = choice.right<A, B, C>(pab)
}
