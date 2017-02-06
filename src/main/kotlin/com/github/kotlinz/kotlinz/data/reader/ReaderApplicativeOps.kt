package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

interface ReaderApplicativeOps<S>: ApplicativeOps<K1<Reader.T, S>>, ReaderMonad<S> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K1<Reader.T, S>, A>) -> Reader<S, B> {
    return { a -> Reader.narrow(a) ap Reader.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K1<Reader.T, S>, A>, K1<K1<Reader.T, S>, B>) -> Reader<S, C> {
    return { a1, a2 ->
      val i1 = Reader.narrow(a1)
      val i2 = Reader.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K1<Reader.T, S>, A>, K1<K1<Reader.T, S>, B>, K1<K1<Reader.T, S>, C>) -> Reader<S, D> {
    return { a1, a2, a3 ->
      val i1 = Reader.narrow(a1)
      val i2 = Reader.narrow(a2)
      val i3 = Reader.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
} 
