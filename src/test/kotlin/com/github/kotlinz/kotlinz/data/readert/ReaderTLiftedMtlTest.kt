package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.EitherId
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import com.github.kotlinz.kotlinz.law.MonadStateLaw
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import com.github.kotlinz.kotlinz.law.StateId
import com.github.kotlinz.kotlinz.law.TransformerBases
import com.github.kotlinz.kotlinz.law.TransformerBases.w
import com.github.kotlinz.kotlinz.law.WriterId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ReaderTLiftedMtlTest : DescribeSpec({
    describe("ReaderT lifted MTL instances") {
        context("MonadState over StateT") {
            val instance = object : ReaderTMonadState<StateId, Int, Int> { override val monad = TransformerBases.stateT }
            val observe = { v: K1<*, *> -> TransformerBases.runState((v as ReaderT<StateId, Int, *>).run(10)) }

            it("should satisfy laws") { MonadStateLaw(instance, observe).assertSatisfyingMonadStateLaw(1, 2) { it * 10 } }

            it("should combine environment and state") {
                val program = ReaderT.narrow(instance.bind({ r: Int -> instance.modify { it + r } }, ReaderT.ask(TransformerBases.stateT)))
                observe(program) shouldBe Identity(Pair(Unit, 13))
            }
        }

        context("MonadWriter over WriterT") {
            val instance = object : ReaderTMonadWriter<WriterId, Int, StringMonoid> { override val monad = TransformerBases.writerT }
            val observe = { v: K1<*, *> -> TransformerBases.runWriter((v as ReaderT<WriterId, Int, *>).run(10)) }

            it("should satisfy laws") {
                MonadWriterLaw(instance, w(""), TransformerBases::combine, observe).assertSatisfyingMonadWriterLaw(1, w("a"), w("b"))
            }
        }

        context("MonadError over EitherT") {
            val instance = object : ReaderTMonadError<EitherId, Int, String> { override val monad = TransformerBases.eitherT }
            val observe = { v: K1<*, *> -> TransformerBases.runEither((v as ReaderT<EitherId, Int, *>).run(10)) }

            it("should satisfy laws") { MonadErrorLaw(instance, observe).assertSatisfyingMonadErrorLaw("e", 1) }

            it("should recover with access to the environment") {
                val recovered = instance.handleErrorWith({ e: String -> ReaderT.reader(TransformerBases.eitherT) { r: Int -> "$e$r" } }, instance.raiseError("e"))
                observe(recovered) shouldBe Identity(Either.Right<String, String>("e10"))
            }
        }
    }
})
