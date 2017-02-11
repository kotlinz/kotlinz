package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.MonoidLaw
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object ListMonoidTest : Spek({
    describe("List Monoid") {
        val law = MonoidLaw(ListMonoid::class.java)
        it ("should satisfy laws") {
            law.assertSatisfyingMonoidLaw(
                    listOf("Monoid 1").toListMonoid(),
                    listOf("Monoid 2").toListMonoid(),
                    listOf("Monoid 3").toListMonoid()
            )
        }
    }
})