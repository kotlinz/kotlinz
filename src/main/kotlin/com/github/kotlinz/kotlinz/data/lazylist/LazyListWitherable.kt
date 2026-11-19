package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.type.Witherable

/** Witherable instance for [LazyList]; like traversal, it only terminates for finite lists. */
interface LazyListWitherable: Witherable<LazyList.T>, LazyListTraversable, LazyListFilterable
