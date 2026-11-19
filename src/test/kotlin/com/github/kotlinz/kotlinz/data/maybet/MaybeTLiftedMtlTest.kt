package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.EitherId
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import com.github.kotlinz.kotlinz.law.MonadStateLaw
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import com.github.kotlinz.kotlinz.law.ReaderId
import com.github.kotlinz.kotlinz.law.StateId
import com.github.kotlinz.kotlinz.law.TransformerBases
import com.github.kotlinz.kotlinz.law.TransformerBases.w
import com.github.kotlinz.kotlinz.law.WriterId
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MaybeTLiftedMtlTest : DescribeSpec({
    describe("MaybeT lifted MTL instances") {
        context("MonadState over StateT") {
            val instance = object : MaybeTMonadState<StateId, Int> { override val monad = TransformerBases.stateT }
            val observe = { v: K1<*, *> -> TransformerBases.runState((v as MaybeT<StateId, *>).run) }

            it("should satisfy laws") { MonadStateLaw(instance, observe).assertSatisfyingMonadStateLaw(1, 2) { it * 10 } }

            it("should keep state changes made before None") {
                val program = instance.bind({ _: Unit -> MaybeT.none<StateId, Int>(TransformerBases.stateT) }, instance.put(5))
                observe(program) shouldBe Identity(Pair(Maybe.None<Int>(), 5))
            }
        }

        context("MonadReader over ReaderT") {
            val instance = object : MaybeTMonadReader<ReaderId, Int> { override val monad = TransformerBases.readerT }
            val observe = { v: K1<*, *> -> TransformerBases.runReader((v as MaybeT<ReaderId, *>).run) }

            it("should satisfy laws") {
                MonadReaderLaw(instance, observe).assertSatisfyingMonadReaderLaw(instance.reader { it + 1 }, { it * 2 }, { it - 1 })
            }
        }

        context("MonadWriter over WriterT") {
            val instance = object : MaybeTMonadWriter<WriterId, StringMonoid> { override val monad = TransformerBases.writerT }
            val observe = { v: K1<*, *> -> TransformerBases.runWriter((v as MaybeT<WriterId, *>).run) }

            it("should satisfy laws") {
                MonadWriterLaw(instance, w(""), TransformerBases::combine, observe).assertSatisfyingMonadWriterLaw(1, w("a"), w("b"))
            }

            it("should keep the output of a None computation without censoring it") {
                val program = instance.bind({ _: Unit -> MaybeT.none<WriterId, Int>(TransformerBases.writerT) }, instance.tell(w("a")))
                observe(instance.censor({ w("[${it.value}]") }, program)) shouldBe Identity(Pair(Maybe.None<Int>(), w("a")))
            }
        }

        context("MonadError over EitherT") {
            val instance = object : MaybeTMonadError<EitherId, String> { override val monad = TransformerBases.eitherT }
            val observe = { v: K1<*, *> -> TransformerBases.runEither((v as MaybeT<EitherId, *>).run) }

            it("should satisfy laws") { MonadErrorLaw(instance, observe).assertSatisfyingMonadErrorLaw("e", 1) }

            it("should raise the base error") {
                observe(instance.raiseError<Int>("e")) shouldBe Identity(Either.Left<String, Maybe<Int>>("e"))
            }
        }
    }
})
