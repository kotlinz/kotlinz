package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

// Universal property: (K . G ~> H) is isomorphic to (K ~> Ran<G, H>)

fun <K, G, H> toRan(functor: Functor<K>, s: FromComposition<K, G, H>): NaturalTransformation<K, K2<Ran.T, G, H>> =
  object: NaturalTransformation<K, K2<Ran.T, G, H>> {
    override fun <A> invoke(fa: K1<K, A>): Ran<G, H, A> = object: Ran<G, H, A> {
      override fun <B> runRan(f: (A) -> K1<G, B>): K1<H, B> = s(functor.fmap(f, fa))
    }
  }

fun <K, G, H> fromRan(s: NaturalTransformation<K, K2<Ran.T, G, H>>): FromComposition<K, G, H> =
  object: FromComposition<K, G, H> {
    override fun <A> invoke(v: K1<K, K1<G, A>>): K1<H, A> = gran(Ran.narrow(s(v)))
  }

fun <G, H, A> gran(ran: Ran<G, H, K1<G, A>>): K1<H, A> = ran.runRan { it }
