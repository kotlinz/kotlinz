package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.store.Store

// State<S, A> is isomorphic to Reader<S, Env<S, A>> (right adjoint after left adjoint)
fun <S, A> State<S, A>.toReaderEnv(): Reader<S, K1<K1<Env.T, S>, A>> = Reader { s ->
  val (a, next) = run(s)
  Env(next, a)
}

fun <S, A> Reader<S, K1<K1<Env.T, S>, A>>.toState(): State<S, A> = State { s ->
  val env = Env.narrow(run(s))
  Pair(env.value, env.env)
}

// Store<S, A> is isomorphic to Env<S, Reader<S, A>> (left adjoint after right adjoint)
fun <S, A> Store<S, A>.toEnvReader(): Env<S, K1<K1<Reader.T, S>, A>> = Env(pos, Reader(peek))

fun <S, A> Env<S, K1<K1<Reader.T, S>, A>>.toStore(): Store<S, A> = Store(Reader.narrow(value).run, env)
