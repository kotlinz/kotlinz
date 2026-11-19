package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.EitherId
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import com.github.kotlinz.kotlinz.law.ReaderId
import com.github.kotlinz.kotlinz.law.TransformerBases
import com.github.kotlinz.kotlinz.law.TransformerBases.w
import com.github.kotlinz.kotlinz.law.WriterId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class StateTLiftedMtlTest : DescribeSpec({
    describe("StateT lifted MTL instances") {
        context("MonadReader over ReaderT") {
            val instance = object : StateTMonadReader<ReaderId, Int, Int> { override val monad = TransformerBases.readerT }
            val observe = { v: K1<*, *> -> TransformerBases.runReader((v as StateT<ReaderId, Int, *>).run(10)) }

            it("should satisfy laws") {
                MonadReaderLaw(instance, observe).assertSatisfyingMonadReaderLaw(instance.reader { it + 1 }, { it * 2 }, { it - 1 })
            }
        }

        context("MonadWriter over WriterT") {
            val instance = object : StateTMonadWriter<WriterId, Int, StringMonoid> { override val monad = TransformerBases.writerT }
            val observe = { v: K1<*, *> -> TransformerBases.runWriter((v as StateT<WriterId, Int, *>).run(10)) }

            it("should satisfy laws") {
                MonadWriterLaw(instance, w(""), TransformerBases::combine, observe).assertSatisfyingMonadWriterLaw(1, w("a"), w("b"))
            }

            it("should listen while threading the state") {
                val program = instance.bind({ _: Unit -> instance.tell(w("a")) }, StateT.modify(TransformerBases.writerT) { s: Int -> s + 1 })
                observe(instance.listen(program)) shouldBe Identity(Pair(Pair(Pair(Unit, w("a")), 11), w("a")))
            }
        }

        context("MonadError over EitherT") {
            val instance = object : StateTMonadError<EitherId, Int, String> { override val monad = TransformerBases.eitherT }
            val observe = { v: K1<*, *> -> TransformerBases.runEither((v as StateT<EitherId, Int, *>).run(10)) }

            it("should satisfy laws") { MonadErrorLaw(instance, observe).assertSatisfyingMonadErrorLaw("e", 1) }

            it("should roll back the state on recovery") {
                val failing = instance.bind({ _: Unit -> instance.raiseError<Int>("e") }, StateT.put(TransformerBases.eitherT, 99))
                val recovered = instance.handleErrorWith({ _: String -> StateT.get(TransformerBases.eitherT) }, failing)
                observe(recovered) shouldBe Identity(Either.Right<String, Pair<Int, Int>>(Pair(10, 10)))
            }
        }
    }
})
