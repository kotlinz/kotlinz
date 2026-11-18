package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.reader.ReaderFunctor
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.adjunction.Adjunction

/** The adjunction `Env<S, _> -| Reader<S, _>` (the curry/uncurry adjunction). */
interface EnvReaderAdjunction<S>: Adjunction<K1<Env.T, S>, K1<Reader.T, S>> {
  override val left: Functor<K1<Env.T, S>> get() = object: EnvFunctor<S> {}
  override val right: Functor<K1<Reader.T, S>> get() = object: ReaderFunctor<S> {}

  override fun <A> unit(a: A): Reader<S, K1<K1<Env.T, S>, A>> = Reader { s -> Env(s, a) }

  override fun <A> counit(fga: K1<K1<Env.T, S>, K1<K1<Reader.T, S>, A>>): A {
    val env = Env.narrow(fga)
    return Reader.narrow(env.value).run(env.env)
  }
}
