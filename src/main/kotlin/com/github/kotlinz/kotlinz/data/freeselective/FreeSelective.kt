package com.github.kotlinz.kotlinz.data.freeselective

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.data.const.ConstSelective
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.Selective
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * The (rigid) free selective functor over an arbitrary type constructor `F`.
 *
 * A program is a chain of instructions where each one may be skipped depending on the result of
 * the previous part. Every possible instruction is known up front, so programs can be analysed
 * statically ([effects], [necessaryEffects], [analyze]) and later interpreted into any [Selective]
 * with [foldMap]. It sits between [com.github.kotlinz.kotlinz.data.freeap.FreeAp] (no branching)
 * and [com.github.kotlinz.kotlinz.data.free.Free] (arbitrary, opaque branching).
 *
 * Instances: [FreeSelectiveFunctor], [FreeSelectiveSelective].
 *
 * ```kotlin
 * val s = FreeSelective.selective<Read.T>()
 * val program = s.ifS(read("verbose") { it == "yes" }, read("log") { it }, FreeSelective.pure("quiet"))
 * program.effects()          // [verbose, log]: every instruction that may run
 * program.necessaryEffects() // [verbose]: the instructions that always run
 * program.foldMap(config, object: MaybeSelective {})
 * ```
 */
sealed class FreeSelective<F, A>: K2<FreeSelective.T, F, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [FreeSelective]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): FreeSelective<F, A> = v as FreeSelective<F, A>
    /** Wraps a value into a program with no effects. */
    fun <F, A> pure(v: A): FreeSelective<F, A> = Pure(v)
    /** Lifts a single instruction into a program that always runs it. */
    fun <F, A> lift(fa: K1<F, A>): FreeSelective<F, A> = Select(Pure(Either.Left(Unit)), fa) { a, _ -> a }
    /** Returns the [Selective] instance for [FreeSelective]. */
    fun <F> selective(): FreeSelectiveSelective<F> = object: FreeSelectiveSelective<F> {}
  }

  /** A program with no effects returning [value]. */
  class Pure<F, A>(val value: A): FreeSelective<F, A>() {
    override fun <B> fmap(f: (A) -> B): FreeSelective<F, B> = Pure(f(value))
    override fun <C, B> selectWith(x: FreeSelective<F, Either<C, B>>, asFunction: (A) -> (C) -> B): FreeSelective<F, B> =
      x.fmap { e -> e.merge { c -> asFunction(value)(c) } }
    override fun <G> foldMap(nt: NaturalTransformation<F, G>, selective: Selective<G>): K1<G, A> = selective.pure(value)
    override fun effects(): List<K1<F, *>> = listOf()
    override fun necessaryEffects(): List<K1<F, *>> = listOf()
    override fun <G> hoist(nt: NaturalTransformation<F, G>): FreeSelective<G, A> = Pure(value)
  }

  /** Runs [condition]; on `Left(x)` runs [effect] and combines its result `y` into `combine(y, x)`, on `Right(a)` skips it. */
  class Select<F, X, Y, A>(val condition: FreeSelective<F, Either<X, A>>, val effect: K1<F, Y>, val combine: (Y, X) -> A): FreeSelective<F, A>() {
    override fun <B> fmap(f: (A) -> B): FreeSelective<F, B> =
      Select(condition.fmap { e -> e.mapRight(f) }, effect) { y, x -> f(combine(y, x)) }

    override fun <C, B> selectWith(x: FreeSelective<F, Either<C, B>>, asFunction: (A) -> (C) -> B): FreeSelective<F, B> {
      val pending = condition.fmap { e -> { c: C -> pairOrApply(e, c, asFunction) } }
      val nested: FreeSelective<F, Either<C, Either<Pair<X, C>, B>>> = x.fmap { e -> e.nestRight() }
      val inner: FreeSelective<F, Either<Pair<X, C>, B>> = pending.selectWith(nested) { it }
      return Select(inner, effect) { y: Y, p: Pair<X, C> -> asFunction(combine(y, p.first))(p.second) }
    }

    override fun <G> foldMap(nt: NaturalTransformation<F, G>, selective: Selective<G>): K1<G, A> =
      selective.select(condition.foldMap(nt, selective), selective.fmap({ y: Y -> { x: X -> combine(y, x) } }, nt(effect)))

    override fun effects(): List<K1<F, *>> = condition.effects() + listOf(effect)
    override fun necessaryEffects(): List<K1<F, *>> = condition.necessaryEffects() + listOfNotNull(effect.takeIf { (condition as? Pure<*, *>)?.value is Either.Left<*, *> })

    override fun <G> hoist(nt: NaturalTransformation<F, G>): FreeSelective<G, A> = Select(condition.hoist(nt), nt(effect), combine)

    private fun <C, B> pairOrApply(e: Either<X, A>, c: C, asFunction: (A) -> (C) -> B): Either<Pair<X, C>, B> = when (e) {
      is Either.Left -> Either.Left(Pair(e.value, c))
      is Either.Right -> Either.Right(asFunction(e.value)(c))
    }
  }

  /** Maps the result of this program without adding effects. */
  abstract infix fun <B> fmap(f: (A) -> B): FreeSelective<F, B>
  /** Interprets every instruction into [selective] using [nt]; skipped branches are skipped by the target. */
  abstract fun <G> foldMap(nt: NaturalTransformation<F, G>, selective: Selective<G>): K1<G, A>
  /** Translates the instruction set from `F` to `G` without interpreting. */
  abstract fun <G> hoist(nt: NaturalTransformation<F, G>): FreeSelective<G, A>
  /** Every instruction that may run, in program order (an over-approximation). */
  abstract fun effects(): List<K1<F, *>>
  /** The instructions that run in every execution (an under-approximation). */
  abstract fun necessaryEffects(): List<K1<F, *>>

  /** Uses this program, turned into a function by [asFunction], as the handler of `Left` results of [x]. */
  internal abstract fun <C, B> selectWith(x: FreeSelective<F, Either<C, B>>, asFunction: (A) -> (C) -> B): FreeSelective<F, B>

  /** Interprets the program back into `F` itself when `F` is already a [Selective]. */
  fun retract(selective: Selective<F>): K1<F, A> = foldMap(NaturalTransformation.identity(), selective)

  /** Statically folds every possible instruction into the monoid [type] via [nt], without running the program. */
  fun <M: Monoid<*, M>> analyze(type: Class<M>, nt: NaturalTransformation<F, K1<Const.T, M>>): M =
    Const.narrow(foldMap(nt, object: ConstSelective<M> { override val type = type })).value
}

private fun <L, R, B> Either<L, R>.mapRight(f: (R) -> B): Either<L, B> = when (this) {
  is Either.Left -> Either.Left(value)
  is Either.Right -> Either.Right(f(value))
}

private fun <L, R> Either<L, R>.merge(onLeft: (L) -> R): R = when (this) {
  is Either.Left -> onLeft(value)
  is Either.Right -> value
}

private fun <L, E, R> Either<L, R>.nestRight(): Either<L, Either<E, R>> = when (this) {
  is Either.Left -> Either.Left(value)
  is Either.Right -> Either.Right(Either.Right(value))
}
