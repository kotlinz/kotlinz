package com.github.kotlinz.kotlinz

operator fun <A, B, C> ((A, B) -> C).invoke(a: A): (B) -> C {
  return { b -> this(a, b) }
}

operator fun <A, B, C, D> ((A, B, C) -> D).invoke(a: A): (B, C) -> D {
  return { b, c -> this(a, b, c) }
}

operator fun <A, B, C, D, E> ((A, B, C, D) -> E).invoke(a: A): (B, C, D) -> E {
  return { b, c, d -> this(a, b, c, d) }
}

operator fun <A, B, C, D, E, F> ((A, B, C, D, E) -> F).invoke(a: A): (B, C, D, E) -> F {
  return { b, c, d, e -> this(a, b, c, d, e) }
}

operator fun <A, B, C, D, E, F, G> ((A, B, C, D, E, F) -> G).invoke(a: A): (B, C, D, E, F) -> G {
  return { b, c, d, e, f -> this(a, b, c, d, e, f) }
}

operator fun <A, B, C, D, E, F, G, H> ((A, B, C, D, E, F, G) -> H).invoke(a: A): (B, C, D, E, F, G) -> H {
  return { b, c, d, e, f, g -> this(a, b, c, d, e, f, g) }
}

operator fun <A, B, C, D, E, F, G, H, I> ((A, B, C, D, E, F, G, H) -> I).invoke(a: A): (B, C, D, E, F, G, H) -> I {
  return { b, c, d, e, f, g, h -> this(a, b, c, d, e, f, g, h) }
}

operator fun <A, B, C, D, E, F, G, H, I, J> ((A, B, C, D, E, F, G, H, I) -> J).invoke(a: A): (B, C, D, E, F, G, H, I) -> J {
  return { b, c, d, e, f, g, h, i -> this(a, b, c, d, e, f, g, h, i) }
}

fun <A, B, C> ((A, B) -> C).curried(): (A) -> (B) -> C {
  return { a -> { b -> this(a, b) } }
}

fun <A, B, C, D> ((A, B, C) -> D).curried(): (A) -> (B) -> (C) -> D {
  return { a -> { b -> { c -> this(a, b, c) } } }
}

fun <A, B, C, D, E> ((A, B, C, D) -> E).curried(): (A) -> (B) -> (C) -> (D) -> E {
  return { a -> { b -> { c -> { d -> this(a, b, c, d) } } } }
}

fun <A, B, C, D, E, F> ((A, B, C, D, E) -> F).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> F {
  return { a -> { b -> { c -> { d -> { e -> this(a, b, c, d, e) } } } } }
}

fun <A, B, C, D, E, F, G> ((A, B, C, D, E, F) -> G).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> (F) -> G {
  return { a -> { b -> { c -> { d -> { e -> { f -> this(a, b, c, d, e, f) } } } } } }
}

fun <A, B, C, D, E, F, G, H> ((A, B, C, D, E, F, G) -> H).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> (F) -> (G) -> H {
  return { a -> { b -> { c -> { d -> { e -> { f -> { g -> this(a, b, c, d, e, f, g) } } } } } } }
}

fun <A, B, C, D, E, F, G, H, I> ((A, B, C, D, E, F, G, H) -> I).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> (F) -> (G) -> (H) -> I {
  return { a -> { b -> { c -> { d -> { e -> { f -> { g -> { h -> this(a, b, c, d, e, f, g, h) } } } } } } } }
}

fun <A, B, C, D, E, F, G, H, I, J> ((A, B, C, D, E, F, G, H, I) -> J).curried(): (A) -> (B) -> (C) -> (D) -> (E) -> (F) -> (G) -> (H) -> (I) -> J {
  return { a -> { b -> { c -> { d -> { e -> { f -> { g -> { h -> { i -> this(a, b, c, d, e, f, g, h, i) } } } } } } } } }
}
