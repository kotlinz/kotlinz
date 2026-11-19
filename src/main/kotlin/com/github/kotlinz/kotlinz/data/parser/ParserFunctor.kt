package com.github.kotlinz.kotlinz.data.parser

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Parser]; maps the parsed value and leaves failures untouched. */
interface ParserFunctor: Functor<Parser.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Parser.T, A>): Parser<B> = Parser { input, position ->
    when (val r = Parser.narrow(v).run(input, position)) {
      is ParseResult.Success -> ParseResult.Success(f(r.value), r.position, r.hint)
      is ParseResult.Failure -> r
    }
  }
}
