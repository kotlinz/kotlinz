package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Witherable
import com.github.kotlinz.kotlinz.type.group.Monoid

/** Witherable instance for [Either] with a monoidal left type. */
interface EitherWitherable<L: Monoid<*, L>>: Witherable<K1<Either.T, L>>, EitherTraversable<L>, EitherFilterable<L>
