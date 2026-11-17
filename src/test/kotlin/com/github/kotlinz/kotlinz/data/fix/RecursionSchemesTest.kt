package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias IntListF = K1<ListF.T, Int>

private val listFunctor = object : ListFFunctor<Int> {}
private val natFunctor = object : NatFFunctor {}

private val sum: Algebra<IntListF, Int> = { v ->
    when (val list = ListF.narrow(v)) {
        is ListF.Nil -> 0
        is ListF.Cons -> list.head + list.tail
    }
}

private val product: Algebra<IntListF, Int> = { v ->
    when (val list = ListF.narrow(v)) {
        is ListF.Nil -> 1
        is ListF.Cons -> list.head * list.tail
    }
}

private val countDown: Coalgebra<IntListF, Int> = { n -> if (n <= 0) ListF.Nil() else ListF.Cons(n, n - 1) }

private val toNat: Coalgebra<NatF.T, Int> = { n -> if (n <= 0) NatF.Zero() else NatF.Succ(n - 1) }

private val toInt: Algebra<NatF.T, Int> = { v ->
    when (val nat = NatF.narrow(v)) {
        is NatF.Zero -> 0
        is NatF.Succ -> nat.prev + 1
    }
}

private val factorial: RAlgebra<NatF.T, Int> = { v ->
    when (val nat = NatF.narrow(v)) {
        is NatF.Zero -> 1
        is NatF.Succ -> (nat.prev.first.cata(natFunctor, toInt) + 1) * nat.prev.second
    }
}

private val suffixes: RAlgebra<IntListF, List<Int>> = { v ->
    when (val list = ListF.narrow(v)) {
        is ListF.Nil -> emptyList()
        is ListF.Cons -> listOf(list.tail.first.cata(listFunctor, sum)) + list.tail.second
    }
}

private fun insert(x: Int): RCoalgebra<IntListF, Fix<IntListF>> = { fix ->
    when (val list = ListF.narrow(fix.unfix)) {
        is ListF.Nil -> ListF.Cons(x, Either.Left(Fix(ListF.Nil())))
        is ListF.Cons -> if (x <= list.head) ListF.Cons(x, Either.Left(fix)) else ListF.Cons(list.head, Either.Right(list.tail))
    }
}

class RecursionSchemesTest : DescribeSpec({
    describe("Base functors") {
        it("ListF should satisfy functor laws") {
            FunctorLaw(listFunctor).assertSatisfyingFunctorLaw(ListF.Cons(1, 2), { it + 1 }, { it.toString() })
        }
        it("NatF should satisfy functor laws") {
            FunctorLaw(natFunctor).assertSatisfyingFunctorLaw(NatF.Succ(1), { it + 1 }, { it.toString() })
        }
    }

    describe("Recursion schemes") {
        it("cata should fold a list") { fixListOf(1, 2, 3, 4).cata(listFunctor, sum) shouldBe 10 }
        it("cata should fold an empty list") { fixListOf<Int>().cata(listFunctor, sum) shouldBe 0 }
        it("ana should unfold a list") { Fix.ana(listFunctor, countDown, 3) shouldBe fixListOf(3, 2, 1) }
        it("ana should unfold a natural number") { cata(natFunctor, toInt)(Fix.ana(natFunctor, toNat, 5)) shouldBe 5 }
        it("hylo should compute factorial without building the structure") { hylo(listFunctor, product, countDown)(5) shouldBe 120 }

        it("hylo should equal cata after ana") {
            hylo(listFunctor, sum, countDown)(10) shouldBe cata(listFunctor, sum)(ana(listFunctor, countDown)(10))
        }

        it("para should compute factorial with access to substructures") {
            Fix.ana(natFunctor, toNat, 5).para(natFunctor, factorial) shouldBe 120
        }

        it("para should collect sums of suffixes") { fixListOf(1, 2, 3).para(listFunctor, suffixes) shouldBe listOf(5, 3, 0) }
        it("apo should insert into a sorted list") { Fix.apo(listFunctor, insert(3), fixListOf(1, 2, 4, 5)) shouldBe fixListOf(1, 2, 3, 4, 5) }
        it("apo should insert at the end") { Fix.apo(listFunctor, insert(9), fixListOf(1, 2)) shouldBe fixListOf(1, 2, 9) }
        it("should handle moderately deep structures") { hylo(natFunctor, toInt, toNat)(1000) shouldBe 1000 }
    }
})
