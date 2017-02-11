package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.MonoidLaw
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object AllMonoidTest : Spek({
    describe("All Monoid") {
        val law = MonoidLaw(AllMonoid::class.java)
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAllMonoid(), true.toAllMonoid(), false.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAllMonoid(), false.toAllMonoid(), true.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAllMonoid(), true.toAllMonoid(), false.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAllMonoid(), false.toAllMonoid(), true.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAllMonoid(), false.toAllMonoid(), false.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAllMonoid(), true.toAllMonoid(), true.toAllMonoid()) }
    }
})