package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.eval.Eval
import com.github.kotlinz.kotlinz.data.eval.EvalMonadRec
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonadRec
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonadRec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun next(n: Int): Either<Int, String> = if (n == 0) Either.Right("done") else Either.Left(n - 1)

class MonadRecTest : DescribeSpec({
    describe("MonadRec") {
        val n = 1_000_000

        it("should loop in Identity without growing the stack") {
            object : IdentityMonadRec {}.tailRecM(n) { Identity(next(it)) } shouldBe Identity("done")
        }

        it("should loop in Eval without growing the stack") {
            Eval.narrow(object : EvalMonadRec {}.tailRecM(n) { Eval.now(next(it)) }).value shouldBe "done"
        }

        it("should loop in State and thread the state") {
            val loop = object : StateMonadRec<Int> {}.tailRecM(n) { x -> State { s: Int -> Pair(next(x), s + 1) } }
            State.narrow(loop).run(0) shouldBe Pair("done", n + 1)
        }
    }
})
