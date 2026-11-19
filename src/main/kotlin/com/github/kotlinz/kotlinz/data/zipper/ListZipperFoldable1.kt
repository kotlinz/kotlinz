package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.type.Foldable1

/** Foldable1 instance for [ListZipper], reducing every element in list order. */
interface ListZipperFoldable1: Foldable1<ListZipper.T>, ListZipperFoldable
