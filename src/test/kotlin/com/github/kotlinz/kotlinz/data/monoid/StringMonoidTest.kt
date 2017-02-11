package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.MonoidLaw
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object StringMonoidTest : Spek({
    describe("String Monoid") {
        val law = MonoidLaw(StringMonoid::class.java)
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw("Monoid 1".toStringMonoid(), "Monoid 2".toStringMonoid(), "Monoid 3".toStringMonoid()) }
    }
})
