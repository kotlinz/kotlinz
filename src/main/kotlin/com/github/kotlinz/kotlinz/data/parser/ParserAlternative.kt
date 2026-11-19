package com.github.kotlinz.kotlinz.data.parser

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative

/**
 * Alternative instance for [Parser] with full backtracking.
 *
 * [orElse] retries the second parser from the original position whenever the first fails, even after
 * consuming input; the first failure is merged (see [ParseError.merge]) into the second result.
 */
interface ParserAlternative: Alternative<Parser.T>, ParserApplicative {
  override fun <A> empty(): Parser<A> = Parser { _, position -> ParseResult.Failure(ParseError(position, emptySet())) }

  override fun <A> orElse(v1: K1<Parser.T, A>, v2: K1<Parser.T, A>): Parser<A> = Parser { input, position ->
    when (val r1 = Parser.narrow(v1).run(input, position)) {
      is ParseResult.Success -> r1
      is ParseResult.Failure -> r1.error.prependTo(Parser.narrow(v2).run(input, position))
    }
  }
}
