package com.github.kotlinz.kotlinz.data.parser

internal fun satisfyParser(expected: String, predicate: (Char) -> Boolean): Parser<Char> = Parser { input, position ->
  input.getOrNull(position)?.takeIf(predicate)?.let { ParseResult.Success(it, position + 1) } ?: failure(position, expected)
}

internal fun stringParser(s: String): Parser<String> = Parser { input, position ->
  if (input.startsWith(s, position)) ParseResult.Success(s, position + s.length) else failure(position, "\"$s\"")
}

internal fun eofParser(): Parser<Unit> = Parser { input, position ->
  if (position >= input.length) ParseResult.Success(Unit, position) else failure(position, "end of input")
}

internal fun <A> deferParser(f: () -> Parser<A>): Parser<A> {
  val parser by lazy(f)
  return Parser { input, position -> parser.run(input, position) }
}

internal fun <A> manyParser(p: Parser<A>): Parser<List<A>> = Parser { input, position ->
  val attempts = generateSequence(Attempt(position, p.run(input, position))) { it.next(p, input) }.toList()
  val steps = attempts.mapNotNull { it.progress }
  val hint = (steps.map { it.hint } + attempts.last().error).reduce { a, b -> a mergeHint b }
  success(steps.map { it.value }, steps.lastOrNull()?.position ?: position, hint)
}

internal fun <A> relabel(result: ParseResult<A>, start: Int, name: String): ParseResult<A> {
  val labelled = ParseError(start, setOfNotNull(name.ifEmpty { null }))
  return when (result) {
    is ParseResult.Failure -> if (result.error.position == start) ParseResult.Failure(labelled) else result
    is ParseResult.Success -> if (result.hint?.position == start) success(result.value, result.position, labelled) else result
  }
}

private class Attempt<A>(val start: Int, val result: ParseResult<A>) {
  val progress: ParseResult.Success<A>? get() = (result as? ParseResult.Success)?.takeIf { it.position > start }
  val error: ParseError? get() = (result as? ParseResult.Failure)?.error ?: (result as ParseResult.Success).hint

  fun next(p: Parser<A>, input: String): Attempt<A>? = progress?.let { Attempt(it.position, p.run(input, it.position)) }
}
