package com.github.kotlinz.kotlinz.data.freecategory

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * A polymorphic mapping from the arrows of a quiver `P` (encoded as `K2<P, A, B>`) to the arrows
 * of a target `K3<T, F, A, B>`, such as a [com.github.kotlinz.kotlinz.type.category.Category].
 *
 * Used to interpret [FreeCategory] and [com.github.kotlinz.kotlinz.data.freearrow.FreeArrow].
 */
interface QuiverTransformation<P, T, F> {
  /** Maps a single quiver arrow to a target arrow with the same endpoints. */
  operator fun <A, B> invoke(arrow: K2<P, A, B>): K3<T, F, A, B>
}
