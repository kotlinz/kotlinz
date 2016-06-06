package com.github.kotlinz.kotlinz

interface K1<T, A> {}
interface K2<T, A, B>: K1<K1<T, A>, B> {}
interface K3<T, A, B, C>: K1<K2<T, A, B>, C> {}
interface K4<T, A, B, C, D>: K1<K3<T, A, B, C>, D> {}
interface K5<T, A, B, C, D, E>: K1<K4<T, A, B, C, D>, E> {}
