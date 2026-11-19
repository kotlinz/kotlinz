package com.github.kotlinz.kotlinz.data.freecategory

import com.github.kotlinz.kotlinz.type.category.Category

/** Category instance for [FreeCategory]; [id] is the empty path. */
interface FreeCategoryCategory<P>: Category<FreeCategory.T, P>, FreeCategoryCompose<P> {
  override fun <A> id(): FreeCategory<P, A, A> = FreeCategory.Id()
}
