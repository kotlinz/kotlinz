package com.github.kotlinz.kotlinz.data.freer

import com.github.kotlinz.kotlinz.K1

/** The first step of a [Freer] program (Operational's `ProgramView`). */
sealed class FreerView<F, A> {
  /** The program has finished with [value]. */
  class Return<F, A>(val value: A): FreerView<F, A>()
  /** The program runs [instruction] and continues with [continuation]. */
  class Bind<F, X, A>(val instruction: K1<F, X>, val continuation: (X) -> Freer<F, A>): FreerView<F, A>()
}
