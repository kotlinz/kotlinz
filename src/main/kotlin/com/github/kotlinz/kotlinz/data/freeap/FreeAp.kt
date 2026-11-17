package com.github.kotlinz.kotlinz.data.freeap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

// Free applicative: effects are a static list, so programs can be inspected without running them.
sealed class FreeAp<F, A>: K2<FreeAp.T, F, A> {
  class T

  companion object {
    fun <F, A> narrow(v: K1<K1<T, F>, A>): FreeAp<F, A> = v as FreeAp<F, A>
    fun <F, A> pure(v: A): FreeAp<F, A> = Pure(v)
    fun <F, A> lift(fa: K1<F, A>): FreeAp<F, A> = Ap(fa, Pure { a: A -> a })
    fun <F> applicative(): FreeApApplicative<F> = object: FreeApApplicative<F> {}
  }

  class Pure<F, A>(val value: A): FreeAp<F, A>() {
    override fun <B> fmap(f: (A) -> B): FreeAp<F, B> = Pure(f(value))
    override fun <B, C> applyTo(v: FreeAp<F, B>, asFunction: (A) -> (B) -> C): FreeAp<F, C> = v.fmap(asFunction(value))
    override fun <G> foldMap(nt: NaturalTransformation<F, G>, applicative: Applicative<G>): K1<G, A> = applicative.pure(value)
    override fun <G> hoist(nt: NaturalTransformation<F, G>): FreeAp<G, A> = Pure(value)
  }

  class Ap<F, X, A>(val fx: K1<F, X>, val next: FreeAp<F, (X) -> A>): FreeAp<F, A>() {
    override fun <B> fmap(f: (A) -> B): FreeAp<F, B> = Ap(fx, next.fmap { g -> { x: X -> f(g(x)) } })

    override fun <B, C> applyTo(v: FreeAp<F, B>, asFunction: (A) -> (B) -> C): FreeAp<F, C> =
      Ap(fx, next.applyTo(v) { g -> { b: B -> { x: X -> asFunction(g(x))(b) } } })

    override fun <G> foldMap(nt: NaturalTransformation<F, G>, applicative: Applicative<G>): K1<G, A> =
      applicative.ap(applicative.fmap({ x: X -> { g: (X) -> A -> g(x) } }, nt(fx)), next.foldMap(nt, applicative))

    override fun <G> hoist(nt: NaturalTransformation<F, G>): FreeAp<G, A> = Ap(nt(fx), next.hoist(nt))
  }

  abstract infix fun <B> fmap(f: (A) -> B): FreeAp<F, B>
  abstract fun <G> foldMap(nt: NaturalTransformation<F, G>, applicative: Applicative<G>): K1<G, A>
  abstract fun <G> hoist(nt: NaturalTransformation<F, G>): FreeAp<G, A>

  // Applies this program, whose value is turned into a function by asFunction, to v (effects of this come first)
  internal abstract fun <B, C> applyTo(v: FreeAp<F, B>, asFunction: (A) -> (B) -> C): FreeAp<F, C>

  infix fun <B> ap(f: FreeAp<F, (A) -> B>): FreeAp<F, B> = f.applyTo(this) { g -> g }
  fun retract(applicative: Applicative<F>): K1<F, A> = foldMap(NaturalTransformation.identity(), applicative)

  fun <M: Monoid<*, M>> analyze(type: Class<M>, nt: NaturalTransformation<F, K1<Const.T, M>>): M =
    Const.narrow(foldMap(nt, Const.applicative(type))).value
}
