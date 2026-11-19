package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.type.Witherable

/** Witherable instance for [ListK]. */
interface ListKWitherable: Witherable<ListK.T>, ListKTraversable, ListKFilterable
