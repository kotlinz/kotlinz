package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.type.category.Category

/** Category instance for [Mealy]; [id] echoes its inputs. */
interface MealyCategory: Category<Mealy.T, Unit>, MealyCompose {
  override fun <A> id(): Mealy<A, A> = Mealy { a -> Pair(a, id()) }
}
