package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.type.Witherable

/** Witherable instance for [Chain]. */
interface ChainWitherable: Witherable<Chain.T>, ChainTraversable, ChainFilterable
