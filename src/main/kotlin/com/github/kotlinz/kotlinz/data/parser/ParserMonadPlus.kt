package com.github.kotlinz.kotlinz.data.parser

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadPlus

/** MonadPlus instance for [Parser]. */
interface ParserMonadPlus: MonadPlus<Parser.T>, ParserMonad, ParserAlternative {
  override fun <A> mzero(): Parser<A> = empty()
  override fun <A> mplus(m1: K1<Parser.T, A>, m2: K1<Parser.T, A>): Parser<A> = orElse(m1, m2)
}
