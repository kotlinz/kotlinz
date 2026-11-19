package com.github.kotlinz.kotlinz.data.freecategory

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Compose

/** Compose instance for [FreeCategory]: concatenates paths. */
interface FreeCategoryCompose<P>: Compose<FreeCategory.T, P> {
  override fun <A, B, C> compose(f: K3<FreeCategory.T, P, B, C>, g: K3<FreeCategory.T, P, A, B>): FreeCategory<P, A, C> =
    FreeCategory.Compose(FreeCategory.narrow(f), FreeCategory.narrow(g))
}
