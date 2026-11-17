package com.github.kotlinz.kotlinz.data.cofree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityFunctor
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias Rose<A> = Cofree<ListK.T, A>

private fun normalize(v: Any?): Any? {
    if (v !is Cofree<*, *>) return v
    return Pair(normalize(v.head), (v.tail as ListK<*>).value.map(::normalize))
}

private fun roseEq(a: Any?, b: Any?): Boolean = normalize(a) == normalize(b)

private fun <A> leaf(a: A): Rose<A> = Cofree(a) { listKOf() }
private fun <A> node(a: A, vararg children: Rose<A>): Rose<A> = Cofree(a) { ListK(children.toList()) }

private fun <A> Cofree<Identity.T, A>.take(n: Int): List<A> =
    generateSequence(this) { Identity.narrow(it.tail).value }.take(n).map { it.head }.toList()

private fun Cofree<Maybe.T, Int>.toList(): List<Int> = listOf(head) + when (val next = Maybe.narrow(tail)) {
    is Maybe.Just -> next.value.toList()
    is Maybe.None -> emptyList()
}

class CofreeTest : DescribeSpec({
    describe("Cofree") {
        val tree = node(1, node(2, leaf(3)), leaf(4))
        val comonad = Cofree.comonad(object : ListKFunctor {})

        context("as Functor") {
            val law = FunctorLaw(comonad) { a, b -> roseEq(a, b) }

            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(tree, { it + 1 }, { it.toString() }) }
        }

        context("as Comonad") {
            val law = ComonadLaw(comonad, ::roseEq)

            it("should satisfy laws with a rose tree") { law.assertSatisfyingComonadLaw(tree) }
            it("should satisfy laws with a leaf") { law.assertSatisfyingComonadLaw(leaf("leaf")) }
        }

        context("operations") {
            it("should expose head and tail") {
                tree.head shouldBe 1
                ListK.narrow(tree.tail).value.map { it.head } shouldBe listOf(2, 4)
            }

            it("should extract the head") { tree.extract() shouldBe 1 }

            it("should extend with a subtree computation") {
                val sizes = comonad.extend({ w: K1<K1<Cofree.T, ListK.T>, Int> -> normalize(w).toString().count { it == '(' } }, tree)
                normalize(sizes) shouldBe normalize(node(4, node(2, leaf(1)), leaf(1)))
            }

            it("should unfold a finite structure") {
                val countDown = Cofree.unfold(object : MaybeFunctor {}, 3) { n -> Pair(n * 10, if (n > 1) Maybe.Just(n - 1) else Maybe.None()) }
                countDown.toList() shouldBe listOf(30, 20, 10)
            }

            it("should describe an infinite stream lazily with coiter") {
                val naturals = Cofree.coiter(object : IdentityFunctor {}, 0) { Identity(it + 1) }
                naturals.take(5) shouldBe listOf(0, 1, 2, 3, 4)
            }

            it("should map an infinite stream lazily") {
                val naturals = Cofree.coiter(object : IdentityFunctor {}, 0) { Identity(it + 1) }
                Cofree.comonad(object : IdentityFunctor {}).fmap({ n: Int -> n * 2 }, naturals).take(3) shouldBe listOf(0, 2, 4)
            }
        }
    }
})
