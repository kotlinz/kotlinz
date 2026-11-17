package com.github.kotlinz.kotlinz.type.group

interface Band<A, Self: Band<A, Self>>: Semigroup<A, Self>
