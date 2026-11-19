package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.type.Witherable

/** Witherable instance for [Maybe]. */
interface MaybeWitherable: Witherable<Maybe.T>, MaybeTraversable, MaybeFilterable
