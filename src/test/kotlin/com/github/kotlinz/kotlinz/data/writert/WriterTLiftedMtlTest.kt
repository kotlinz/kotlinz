package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.EitherId
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import com.github.kotlinz.kotlinz.law.MonadStateLaw
import com.github.kotlinz.kotlinz.law.ReaderId
import com.github.kotlinz.kotlinz.law.StateId
import com.github.kotlinz.kotlinz.law.TransformerBases
import com.github.kotlinz.kotlinz.law.TransformerBases.w
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class WriterTLiftedMtlTest : DescribeSpec({
    describe("WriterT lifted MTL instances") {
        val type = StringMonoid::class.java

        context("MonadState over StateT") {
            val instance = object : WriterTMonadState<StateId, StringMonoid, Int> {
                override val monad = TransformerBases.stateT
                override val type = type
            }
            val observe = { v: K1<*, *> -> TransformerBases.runState((v as WriterT<StateId, *, *>).run) }

            it("should satisfy laws") { MonadStateLaw(instance, observe).assertSatisfyingMonadStateLaw(1, 2) { it * 10 } }

            it("should log and update the state") {
                val program = instance.bind({ s: Int -> WriterT.tell(TransformerBases.stateT, type, w("s=$s")) }, instance.get())
                observe(program) shouldBe Identity(Pair(Pair(Unit, w("s=3")), 3))
            }
        }

        context("MonadReader over ReaderT") {
            val instance = object : WriterTMonadReader<ReaderId, StringMonoid, Int> {
                override val monad = TransformerBases.readerT
                override val type = type
            }
            val observe = { v: K1<*, *> -> TransformerBases.runReader((v as WriterT<ReaderId, *, *>).run) }

            it("should satisfy laws") {
                MonadReaderLaw(instance, observe).assertSatisfyingMonadReaderLaw(instance.reader { it + 1 }, { it * 2 }, { it - 1 })
            }
        }

        context("MonadError over EitherT") {
            val instance = object : WriterTMonadError<EitherId, StringMonoid, String> {
                override val monad = TransformerBases.eitherT
                override val type = type
            }
            val observe = { v: K1<*, *> -> TransformerBases.runEither((v as WriterT<EitherId, *, *>).run) }

            it("should satisfy laws") { MonadErrorLaw(instance, observe).assertSatisfyingMonadErrorLaw("e", 1) }

            it("should recover from a base error") {
                val recovered = instance.handleErrorWith({ e: String -> WriterT.writer(TransformerBases.eitherT, type, Pair(0, w(e))) }, instance.raiseError("boom"))
                observe(recovered) shouldBe Identity(Either.Right<String, Pair<Int, StringMonoid>>(Pair(0, w("boom"))))
            }
        }
    }
})
