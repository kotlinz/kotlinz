package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1

// forall A. K<G<A>> -> H<A>
interface FromComposition<K, G, H> {
  operator fun <A> invoke(v: K1<K, K1<G, A>>): K1<H, A>
}

// forall A. H<A> -> F<G<A>>
interface IntoComposition<H, F, G> {
  operator fun <A> invoke(v: K1<H, A>): K1<F, K1<G, A>>
}
