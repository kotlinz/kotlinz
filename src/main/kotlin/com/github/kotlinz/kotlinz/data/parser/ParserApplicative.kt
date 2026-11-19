package com.github.kotlinz.kotlinz.data.parser

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Parser]; parses the function, then the argument from where it stopped. */
interface ParserApplicative: Applicative<Parser.T>, ParserFunctor {
  override fun <A> pure(v: A): Parser<A> = Parser { _, position -> ParseResult.Success(v, position) }

  override fun <A, B> ap(f: K1<Parser.T, (A) -> B>, v: K1<Parser.T, A>): Parser<B> = Parser { input, position ->
    when (val r = Parser.narrow(f).run(input, position)) {
      is ParseResult.Success -> r.hint.prependTo(fmap(r.value, v).run(input, r.position))
      is ParseResult.Failure -> r
    }
  }
}
