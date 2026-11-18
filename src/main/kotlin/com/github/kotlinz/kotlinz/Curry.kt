package com.github.kotlinz.kotlinz

/**
 * Partially applies the first argument of a 2-ary function: `f(a)` returns `{ b -> f(a, b) }`.
 *
 * The overloads below do the same for functions of up to 9 arguments, so arguments can be
 * supplied one at a time, e.g. `f(a)(b)(c)`.
 */
operator fun <A, B, C> ((A, B) -> C).invoke(a: A): (B) -> C {
  return { b -> this(a, b) }
}

/** Partially applies the first argument of a 3-ary function. */
operator fun <A, B, C, D> ((A, B, C) -> D).invoke(a: A): (B, C) -> D {
  return { b, c -> this(a, b, c) }
}

/** Partially applies the first argument of a 4-ary function. */
operator fun <A, B, C, D, E> ((A, B, C, D) -> E).invoke(a: A): (B, C, D) -> E {
  return { b, c, d -> this(a, b, c, d) }
}

/** Partially applies the first argument of a 5-ary function. */
operator fun <A, B, C, D, E, F> ((A, B, C, D, E) -> F).invoke(a: A): (B, C, D, E) -> F {
  return { b, c, d, e -> this(a, b, c, d, e) }
}

/** Partially applies the first argument of a 6-ary function. */
operator fun <A, B, C, D, E, F, G> ((A, B, C, D, E, F) -> G).invoke(a: A): (B, C, D, E, F) -> G {
  return { b, c, d, e, f -> this(a, b, c, d, e, f) }
}

/** Partially applies the first argument of a 7-ary function. */
operator fun <A, B, C, D, E, F, G, H> ((A, B, C, D, E, F, G) -> H).invoke(a: A): (B, C, D, E, F, G) -> H {
  return { b, c, d, e, f, g -> this(a, b, c, d, e, f, g) }
}

/** Partially applies the first argument of an 8-ary function. */
operator fun <A, B, C, D, E, F, G, H, I> ((A, B, C, D, E, F, G, H) -> I).invoke(a: A): (B, C, D, E, F, G, H) -> I {
  return { b, c, d, e, f, g, h -> this(a, b, c, d, e, f, g, h) }
}

/** Partially applies the first argument of a 9-ary function. */
operator fun <A, B, C, D, E, F, G, H, I, J> ((A, B, C, D, E, F, G, H, I) -> J).invoke(a: A): (B, C, D, E, F, G, H, I) -> J {
  return { b, c, d, e, f, g, h, i -> this(a, b, c, d, e, f, g, h, i) }
}

/**
 * Converts a 2-ary function into a chain of unary functions: `(A, B) -> C` becomes `(A) -> (B) -> C`.
 *
 * The overloads below do the same for functions of up to 9 arguments.
 */
fun <A, B, C> ((A, B) -> C).curried(): (A) -> (B) -> C {
  return { a -> { b -> this(a, b) } }
}

/** Curries a 3-ary function. */
fun <A, B, C, D> ((A, B, C) -> D).curried(): (A) -> (B) -> (C) -> D {
  return { a -> { b -> { c -> this(a, b, c) } } }
}

/** Curries a 4-ary function. */
fun <A, B, C, D, E> ((A, B, C, D) -> E).curried(): (A) -> (B) -> (C) -> (D) -> E {
  return { a -> { b -> { c -> { d -> this(a, b, c, d) } } } }
}

/** Curries a 5-ary function. */
fun <A, B, C, D, E, F> ((A, B, C, D, E) -> F).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> F {
  return { a -> { b -> { c -> { d -> { e -> this(a, b, c, d, e) } } } } }
}

/** Curries a 6-ary function. */
fun <A, B, C, D, E, F, G> ((A, B, C, D, E, F) -> G).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> (F) -> G {
  return { a -> { b -> { c -> { d -> { e -> { f -> this(a, b, c, d, e, f) } } } } } }
}

/** Curries a 7-ary function. */
fun <A, B, C, D, E, F, G, H> ((A, B, C, D, E, F, G) -> H).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> (F) -> (G) -> H {
  return { a -> { b -> { c -> { d -> { e -> { f -> { g -> this(a, b, c, d, e, f, g) } } } } } } }
}

/** Curries an 8-ary function. */
fun <A, B, C, D, E, F, G, H, I> ((A, B, C, D, E, F, G, H) -> I).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> (F) -> (G) -> (H) -> I {
  return { a -> { b -> { c -> { d -> { e -> { f -> { g -> { h -> this(a, b, c, d, e, f, g, h) } } } } } } } }
}

/** Curries a 9-ary function. */
fun <A, B, C, D, E, F, G, H, I, J> ((A, B, C, D, E, F, G, H, I) -> J).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> (F) -> (G) -> (H) -> (I) -> J {
  return { a -> { b -> { c -> { d -> { e -> { f -> { g -> { h -> { i -> this(a, b, c, d, e, f, g, h, i) } } } } } } } } }
}
