package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

// Universal property: (Lan<G, H> ~> F) is isomorphic to (H ~> F . G)

fun <F, G, H> toLan(functor: Functor<F>, s: IntoComposition<H, F, G>): NaturalTransformation<K2<Lan.T, G, H>, F> =
  object: NaturalTransformation<K2<Lan.T, G, H>, F> {
    override fun <A> invoke(fa: K1<K2<Lan.T, G, H>, A>): K1<F, A> {
      val lan = Lan.narrow(fa)
      return functor.fmap(lan.lower, s(lan.value))
    }
  }

fun <F, G, H> fromLan(s: NaturalTransformation<K2<Lan.T, G, H>, F>): IntoComposition<H, F, G> =
  object: IntoComposition<H, F, G> {
    override fun <A> invoke(v: K1<H, A>): K1<F, K1<G, A>> = s(glan<G, H, A>(v))
  }

fun <G, H, A> glan(v: K1<H, A>): Lan<G, H, K1<G, A>> = Lan({ ga: K1<G, A> -> ga }, v)
