package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.eithert.EitherT
import com.github.kotlinz.kotlinz.data.eithert.EitherTMonadIO
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybet.MaybeT
import com.github.kotlinz.kotlinz.data.maybet.MaybeTMonadIO
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.readert.ReaderT
import com.github.kotlinz.kotlinz.data.readert.ReaderTMonadIO
import com.github.kotlinz.kotlinz.data.statet.StateT
import com.github.kotlinz.kotlinz.data.statet.StateTMonadIO
import com.github.kotlinz.kotlinz.data.writert.WriterT
import com.github.kotlinz.kotlinz.data.writert.WriterTMonadIO
import com.github.kotlinz.kotlinz.law.MonadIOLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MonadIOTest : DescribeSpec({
    describe("MonadIO") {
        val io = object : IOMonadIO {}
        val type = StringMonoid::class.java
        val unsafeRun = { v: K1<IO.T, *> -> IO.narrow(v).unsafeRun() }
        val f = { x: Int -> IO { x * 2 } }
        val maybeT = object : MaybeTMonadIO<IO.T> { override val monad = io }
        val stateT = object : StateTMonadIO<K1<MaybeT.T, IO.T>, Int> { override val monad = maybeT }

        it("IO should satisfy laws") { MonadIOLaw(io, unsafeRun).assertSatisfyingMonadIOLaw(1, IO { 1 }, f) }

        it("MaybeT should satisfy laws") {
            MonadIOLaw(maybeT) { unsafeRun((it as MaybeT<IO.T, *>).run) }.assertSatisfyingMonadIOLaw(1, IO { 1 }, f)
        }

        it("EitherT should satisfy laws") {
            val instance = object : EitherTMonadIO<IO.T, String> { override val monad = io }
            MonadIOLaw(instance) { unsafeRun((it as EitherT<IO.T, *, *>).run) }.assertSatisfyingMonadIOLaw(1, IO { 1 }, f)
        }

        it("ReaderT should satisfy laws") {
            val instance = object : ReaderTMonadIO<IO.T, Int> { override val monad = io }
            MonadIOLaw(instance) { unsafeRun((it as ReaderT<IO.T, Int, *>).run(3)) }.assertSatisfyingMonadIOLaw(1, IO { 1 }, f)
        }

        it("WriterT should satisfy laws") {
            val instance = object : WriterTMonadIO<IO.T, StringMonoid> {
                override val monad = io
                override val type = type
            }
            MonadIOLaw(instance) { unsafeRun((it as WriterT<IO.T, *, *>).run) }.assertSatisfyingMonadIOLaw(1, IO { 1 }, f)
        }

        it("StateT over MaybeT should satisfy laws") {
            val run = { v: K1<K2<StateT.T, K1<MaybeT.T, IO.T>, Int>, *> -> unsafeRun(MaybeT.narrow(StateT.narrow(v).run(3)).run) }
            MonadIOLaw(stateT, run).assertSatisfyingMonadIOLaw(1, IO { 1 }, f)
        }

        it("should defer the lifted effect through a stack") {
            var count = 0
            val program = StateT.liftIO<K1<MaybeT.T, IO.T>, Int, Int>(maybeT, IO { count += 1; count }) bind { x ->
                StateT.modify<K1<MaybeT.T, IO.T>, Int>(maybeT) { it + x }
            }
            count shouldBe 0
            unsafeRun(MaybeT.narrow(program.run(10)).run) shouldBe Maybe.Just(Pair(Unit, 11))
            count shouldBe 1
        }

        it("should lift IO with companion helpers") {
            unsafeRun(MaybeT.liftIO(io, IO { 1 }).run) shouldBe Maybe.Just(1)
            unsafeRun(EitherT.liftIO<IO.T, String, Int>(io, IO { 1 }).run) shouldBe Either.Right<String, Int>(1)
            unsafeRun(ReaderT.liftIO<IO.T, Int, Int>(io, IO { 1 }).run(0)) shouldBe 1
            unsafeRun(WriterT.liftIO(io, type, IO { 1 }).run) shouldBe Pair(1, StringMonoid(""))
        }
    }
})
