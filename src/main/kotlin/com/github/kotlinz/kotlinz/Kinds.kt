package com.github.kotlinz.kotlinz

/**
 * Higher-kinded type encoding.
 *
 * Kotlin cannot abstract over type constructors such as `Maybe<_>`, so a value of type
 * `F<A>` is represented as `K1<F.T, A>`, where `T` is a phantom tag class nested in the
 * data type (e.g. `Maybe.T`). Every data type implements `K1` with its own tag:
 *
 * ```kotlin
 * sealed class Maybe<A>: K1<Maybe.T, A> { class T }
 * ```
 *
 * Type classes are written against the tag (`Functor<Maybe.T>`), and instances return the
 * concrete type, which callers recover with the data type's `narrow` function (an unchecked cast
 * that is safe as long as `Maybe` is the only implementor of `K1<Maybe.T, _>`).
 *
 * Types with more parameters use `K2`..`K5`, which are curried left to right:
 * `K2<T, A, B>` is `K1<K1<T, A>, B>`. Partially applying the leading parameters therefore
 * yields a `K1`, so `Either<L, R>` (a `K2<Either.T, L, R>`) is a `Functor<K1<Either.T, L>>`
 * over its right side.
 *
 * @param T the tag identifying the type constructor.
 * @param A the type argument.
 */
interface K1<T, A> {}
/**
 * A type constructor applied to two arguments, encoded as `K1<K1<T, A>, B>`.
 * @see K1
 */
interface K2<T, A, B>: K1<K1<T, A>, B> {}
/**
 * A type constructor applied to three arguments, encoded as `K1<K2<T, A, B>, C>`.
 * @see K1
 */
interface K3<T, A, B, C>: K1<K2<T, A, B>, C> {}
/**
 * A type constructor applied to four arguments, encoded as `K1<K3<T, A, B, C>, D>`.
 * @see K1
 */
interface K4<T, A, B, C, D>: K1<K3<T, A, B, C>, D> {}
/**
 * A type constructor applied to five arguments, encoded as `K1<K4<T, A, B, C, D>, E>`.
 * @see K1
 */
interface K5<T, A, B, C, D, E>: K1<K4<T, A, B, C, D>, E> {}
