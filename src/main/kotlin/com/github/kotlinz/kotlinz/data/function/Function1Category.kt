package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.type.category.Category

/** Category instance for [Function1]. */
interface Function1Category: Category<Function1.T, Unit>, Function1Compose {
  override fun <A> id(): Function1<A, A> = Function1 { a -> a }
}
