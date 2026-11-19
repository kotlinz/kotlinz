package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import com.github.kotlinz.kotlinz.law.MonadStateLaw
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import com.github.kotlinz.kotlinz.law.ReaderId
import com.github.kotlinz.kotlinz.law.StateId
import com.github.kotlinz.kotlinz.law.TransformerBases
import com.github.kotlinz.kotlinz.law.TransformerBases.w
import com.github.kotlinz.kotlinz.law.WriterId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EitherTLiftedMtlTest : DescribeSpec({
    describe("EitherT lifted MTL instances") {
        context("MonadState over StateT") {
            val instance = object : EitherTMonadState<StateId, String, Int> { override val monad = TransformerBases.stateT }
            val observe = { v: K1<*, *> -> TransformerBases.runState((v as EitherT<StateId, *, *>).run) }

            it("should satisfy laws") { MonadStateLaw(instance, observe).assertSatisfyingMonadStateLaw(1, 2) { it * 10 } }

            it("should keep state changes made before an error") {
                val program = instance.bind({ _: Unit -> EitherT.raiseError<StateId, String, Int>(TransformerBases.stateT, "e") }, instance.put(5))
                observe(program) shouldBe Identity(Pair(Either.Left<String, Int>("e"), 5))
            }
        }

        context("MonadReader over ReaderT") {
            val instance = object : EitherTMonadReader<ReaderId, String, Int> { override val monad = TransformerBases.readerT }
            val observe = { v: K1<*, *> -> TransformerBases.runReader((v as EitherT<ReaderId, *, *>).run) }

            it("should satisfy laws") {
                MonadReaderLaw(instance, observe).assertSatisfyingMonadReaderLaw(instance.reader { it + 1 }, { it * 2 }, { it - 1 })
            }
        }

        context("MonadWriter over WriterT") {
            val instance = object : EitherTMonadWriter<WriterId, String, StringMonoid> { override val monad = TransformerBases.writerT }
            val observe = { v: K1<*, *> -> TransformerBases.runWriter((v as EitherT<WriterId, *, *>).run) }

            it("should satisfy laws") {
                MonadWriterLaw(instance, w(""), TransformerBases::combine, observe).assertSatisfyingMonadWriterLaw(1, w("a"), w("b"))
            }

            it("should expose the output with listen") {
                observe(instance.listen(instance.writer(Pair(1, w("a"))))) shouldBe Identity(Pair(Either.Right<String, Pair<Int, StringMonoid>>(Pair(1, w("a"))), w("a")))
            }
        }
    }
})
