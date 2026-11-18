# Distributive and Representable

## Distributive

<!-- nocheck -->
```kotlin
interface Distributive<G>: Functor<G> {
  fun <F, A> distribute(functor: Functor<F>, fga: K1<F, K1<G, A>>): K1<G, K1<F, A>>
  fun <F, A, B> collect(functor: Functor<F>, f: (A) -> K1<G, B>, fa: K1<F, A>): K1<G, K1<F, B>>
  fun <F, A, B> cotraverse(functor: Functor<F>, f: (K1<F, A>) -> B, fga: K1<F, K1<G, A>>): K1<G, B>
}
```

`Distributive` is the dual of `Traversable`: it pulls a `G` out of *any* functor `F`. Only functors with a fixed
"shape", such as functions from a fixed input, can do this. Instances: `Identity`, `Reader<R, _>`.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.reader.ReaderDistributive

// K1 is invariant, so the element type must be widened explicitly
val readers = listKOf<K1<K1<Reader.T, Int>, Int>>(Reader { n: Int -> n + 1 }, Reader { n: Int -> n * 2 })
val distributed = object : ReaderDistributive<Int> {}.distribute(object : ListKFunctor {}, readers)

check(distributed.run(10) == listKOf(11, 20))
```

## Representable

<!-- nocheck -->
```kotlin
interface Representable<F, Rep>: Functor<F> {
  fun <A> tabulate(f: (Rep) -> A): K1<F, A>
  fun <A> index(fa: K1<F, A>): (Rep) -> A
}
```

A representable functor is isomorphic to functions from a fixed type `Rep`: `tabulate` and `index` convert between
the two views.

| Data type | `Rep` |
|---|---|
| `Identity` | `Unit` |
| `Twin` (a pair of two values of the same type) | `Boolean` (`false` = first, `true` = second) |
| `Reader<R, _>` | `R` |

```kotlin
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityRepresentable
import com.github.kotlinz.kotlinz.data.twin.Twin

val twin = Twin.tabulate { isSecond -> if (isSecond) "right" else "left" }
check(twin == Twin("left", "right"))
check(twin.index(true) == "right")

val identity = object : IdentityRepresentable {}
check(identity.tabulate { _: Unit -> 42 } == Identity(42))
check(identity.index(Identity("x"))(Unit) == "x")
```

See also [`Adjunction`](../category-theory.md#adjunction): the right adjoint of an adjunction is always representable.
