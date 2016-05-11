package com.github.unhappychoice.kotlinz

interface K1<µ, A> {}
interface K2<µ, A, B>: K1<K1<µ, A>, B> {}
interface K3<µ, A, B, C>: K1<K2<µ, A, B>, C> {}
interface K4<µ, A, B, C, D>: K1<K3<µ, A, B, C>, D> {}
interface K5<µ, A, B, C, D, E>: K1<K4<µ, A, B, C, D>, E> {}
