package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * A monad transformer that is functorial in its base monad: [hoist] swaps the base `F` for `G`.
 *
 * As with [MonadTrans], an instance fixes both bases; `TF` and `TG` are the transformer applied to `F` and `G`.
 *
 * Laws:
 * - Identity (when `F == G`): `hoist(NaturalTransformation.identity(), v) == v`
 * - Composition: `hoist(f andThen g, v) == hoist(g, hoist(f, v))`
 *
 * @see MonadTrans
 * @see NaturalTransformation
 */
interface MFunctor<F, G, TF, TG> {
  /** Replaces the base monad of `v` by applying `nt` to every base computation it contains. */
  fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<TF, A>): K1<TG, A>
}
