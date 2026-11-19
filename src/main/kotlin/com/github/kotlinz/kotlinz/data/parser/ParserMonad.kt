package com.github.kotlinz.kotlinz.data.parser

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Parser]; the next parser is chosen from the previous result and continues from its position. */
interface ParserMonad: Monad<Parser.T>, ParserApplicative, ParserFunctor {
  override fun <A> join(v: K1<Parser.T, K1<Parser.T, A>>): Parser<A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<Parser.T, B>, v: K1<Parser.T, A>): Parser<B> = Parser { input, position ->
    when (val r = Parser.narrow(v).run(input, position)) {
      is ParseResult.Success -> r.hint.prependTo(Parser.narrow(f(r.value)).run(input, r.position))
      is ParseResult.Failure -> r
    }
  }
}
