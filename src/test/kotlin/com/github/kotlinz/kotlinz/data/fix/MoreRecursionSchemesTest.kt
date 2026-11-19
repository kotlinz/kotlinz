package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.cofree.Cofree
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.free.Free
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias IntListLayer = K1<ListF.T, Int>

private val listFunctor = object : ListFFunctor<Int> {}
private val natFunctor = object : NatFFunctor {}

private val sum: Algebra<IntListLayer, Int> = { v ->
    when (val list = ListF.narrow(v)) {
        is ListF.Nil -> 0
        is ListF.Cons -> list.head + list.tail
    }
}

private val length: Algebra<IntListLayer, Int> = { v ->
    when (val list = ListF.narrow(v)) {
        is ListF.Nil -> 0
        is ListF.Cons -> list.tail + 1
    }
}

private val countDown: Coalgebra<IntListLayer, Int> = { n -> if (n <= 0) ListF.Nil() else ListF.Cons(n, n - 1) }

private val toNat: Coalgebra<NatF.T, Int> = { n -> if (n <= 0) NatF.Zero() else NatF.Succ(n - 1) }

private val fibonacci: CVAlgebra<NatF.T, Int> = { v ->
    when (val nat = NatF.narrow(v)) {
        is NatF.Zero -> 0
        is NatF.Succ -> when (val before = NatF.narrow(nat.prev.tail)) {
            is NatF.Zero -> 1
            is NatF.Succ -> nat.prev.head + before.prev.head
        }
    }
}

private val doubleHeads = object : NaturalTransformation<IntListLayer, IntListLayer> {
    override fun <A> invoke(fa: K1<IntListLayer, A>): K1<IntListLayer, A> = when (val list = ListF.narrow(fa)) {
        is ListF.Nil -> ListF.Nil()
        is ListF.Cons -> ListF.Cons(list.head * 2, list.tail)
    }
}

private val weightByLength: (K1<IntListLayer, Pair<Int, Int>>) -> Int = { v ->
    when (val list = ListF.narrow(v)) {
        is ListF.Nil -> 0
        is ListF.Cons -> list.head * list.tail.first + list.tail.second
    }
}

private val pairs: CVCoalgebra<IntListLayer, Int> = { n -> if (n <= 0) ListF.Nil() else ListF.Cons(n, Free.liftF(ListF.Cons(n * 10, n - 1))) }

class MoreRecursionSchemesTest : DescribeSpec({
    describe("Fold schemes") {
        it("histo should compute fibonacci from the history") { histo(natFunctor, fibonacci)(Fix.ana(natFunctor, toNat, 10)) shouldBe 55 }
        it("histo should equal cata for history-free algebras") {
            histo(listFunctor) { v: K1<IntListLayer, Cofree<IntListLayer, Int>> ->
                sum(listFunctor.fmap({ c -> c.head }, v))
            }(fixListOf(1, 2, 3)) shouldBe 6
        }

        it("zygo should combine an auxiliary fold") { zygo(listFunctor, length, weightByLength)(fixListOf(1, 2, 3, 4)) shouldBe 10 }
        it("prepro should transform deeper layers more often") { prepro(listFunctor, doubleHeads, sum)(fixListOf(1, 2, 3)) shouldBe 17 }
        it("prepro with identity should equal cata") { prepro(listFunctor, NaturalTransformation.identity(), sum)(fixListOf(1, 2, 3)) shouldBe 6 }

        it("elgot should short-circuit the unfold") {
            val coalgebra = { n: Int -> if (n == 2) Either.Left(100) else Either.Right<Int, K1<IntListLayer, Int>>(countDown(n)) }
            elgot(listFunctor, sum, coalgebra)(5) shouldBe 112
            elgot(listFunctor, sum, coalgebra)(1) shouldBe 1
        }
    }

    describe("Unfold schemes") {
        it("futu should emit several layers per step") { futu(listFunctor, pairs)(2) shouldBe fixListOf(2, 20, 1, 10) }

        it("futu should keep unfolding pure leaves") {
            futu(listFunctor) { n: Int -> if (n <= 0) ListF.Nil() else ListF.Cons(n, Free.pure(n - 1)) }(3) shouldBe fixListOf(3, 2, 1)
        }

        it("futu should resume bound programs") {
            val coalgebra: CVCoalgebra<IntListLayer, Int> = { n ->
                if (n <= 0) ListF.Nil()
                else ListF.Cons(n, Free.liftF<IntListLayer, Int>(ListF.Cons(n * 10, n - 1)) bind { m -> Free.liftF(ListF.Cons(m * 100, m)) })
            }
            futu(listFunctor, coalgebra)(1) shouldBe fixListOf(1, 10, 0)
        }

        it("postpro should transform deeper layers more often") { postpro(listFunctor, doubleHeads, countDown)(3) shouldBe fixListOf(3, 4, 4) }

        it("coelgot should see the seed and fold lazily") {
            var unfolded = 0
            val coalgebra: Coalgebra<IntListLayer, Int> = { n -> unfolded++; countDown(n) }
            val algebra = { n: Int, rest: () -> K1<IntListLayer, Int> -> if (n <= 2) 100 else sum(rest()) }
            coelgot(listFunctor, algebra, coalgebra)(5) shouldBe 112
            unfolded shouldBe 3
        }
    }
})
