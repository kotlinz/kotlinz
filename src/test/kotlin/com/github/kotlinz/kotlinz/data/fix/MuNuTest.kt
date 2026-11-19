package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias IntLayer = K1<ListF.T, Int>

private val listFunctor = object : ListFFunctor<Int> {}
private val natFunctor = object : NatFFunctor {}

private val sum: Algebra<IntLayer, Int> = { v ->
    when (val list = ListF.narrow(v)) {
        is ListF.Nil -> 0
        is ListF.Cons -> list.head + list.tail
    }
}

private val countDown: Coalgebra<IntLayer, Int> = { n -> if (n <= 0) ListF.Nil() else ListF.Cons(n, n - 1) }
private val toNat: Coalgebra<NatF.T, Int> = { n -> if (n <= 0) NatF.Zero() else NatF.Succ(n - 1) }

private val toInt: Algebra<NatF.T, Int> = { v ->
    when (val nat = NatF.narrow(v)) {
        is NatF.Zero -> 0
        is NatF.Succ -> nat.prev + 1
    }
}

private fun take(n: Int, nu: Nu<IntLayer>): List<Int> =
    if (n == 0) emptyList()
    else when (val layer = ListF.narrow(nu.project(listFunctor))) {
        is ListF.Nil -> emptyList()
        is ListF.Cons -> listOf(layer.head) + take(n - 1, layer.tail)
    }

class MuNuTest : DescribeSpec({
    describe("Mu") {
        val list = Mu.fromFix(listFunctor, fixListOf(1, 2, 3))

        it("should fold without a functor") { list.cata(sum) shouldBe 6 }
        it("should round-trip through Fix") { list.toFix() shouldBe fixListOf(1, 2, 3) }
        it("should unfold eagerly with ana") { Mu.ana(natFunctor, toNat, 4).cata(toInt) shouldBe 4 }

        it("should embed and project layers") {
            val consed = Mu.embed(listFunctor, ListF.Cons(0, list))
            consed.toFix() shouldBe fixListOf(0, 1, 2, 3)
            when (val layer = ListF.narrow(consed.project(listFunctor))) {
                is ListF.Nil -> error("expected a cons layer")
                is ListF.Cons -> layer.tail.toFix() shouldBe fixListOf(1, 2, 3)
            }
        }

        it("should convert to Nu") { list.toNu(listFunctor).toFix(listFunctor) shouldBe fixListOf(1, 2, 3) }
    }

    describe("Nu") {
        val naturals = Nu.unfold({ n: Int -> ListF.Cons(n, n + 1) }, 0)

        it("should project infinite structures lazily") { take(5, naturals) shouldBe listOf(0, 1, 2, 3, 4) }
        it("should fold finite structures") { Nu.unfold(countDown, 4).cata(listFunctor, sum) shouldBe 10 }
        it("should round-trip through Fix") { Nu.fromFix(fixListOf(1, 2)).toFix(listFunctor) shouldBe fixListOf(1, 2) }
        it("should convert to Mu") { Nu.unfold(countDown, 3).toMu(listFunctor).cata(sum) shouldBe 6 }

        it("should embed a layer in front") {
            val prefixed = Nu.embed(listFunctor, ListF.Cons(-1, naturals))
            take(3, prefixed) shouldBe listOf(-1, 0, 1)
        }

        it("should agree with Mu on finite values") {
            Nu.unfold(countDown, 5).cata(listFunctor, sum) shouldBe Mu.ana(listFunctor, countDown, 5).cata(sum)
        }
    }
})
