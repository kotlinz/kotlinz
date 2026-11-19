package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.eithert.EitherT
import com.github.kotlinz.kotlinz.data.eithert.EitherTMonadError
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.readert.ReaderT
import com.github.kotlinz.kotlinz.data.readert.ReaderTMonadReader
import com.github.kotlinz.kotlinz.data.statet.StateT
import com.github.kotlinz.kotlinz.data.statet.StateTMonadState
import com.github.kotlinz.kotlinz.data.writert.WriterT
import com.github.kotlinz.kotlinz.data.writert.WriterTMonadWriter

typealias StateId = K2<StateT.T, Identity.T, Int>
typealias ReaderId = K2<ReaderT.T, Identity.T, Int>
typealias WriterId = K2<WriterT.T, Identity.T, StringMonoid>
typealias EitherId = K2<EitherT.T, Identity.T, String>

/** Identity-based MTL monads used as bases when testing lifted transformer instances. */
object TransformerBases {
    val identity = object : IdentityMonad {}
    val stateT = object : StateTMonadState<Identity.T, Int> { override val monad = identity }
    val readerT = object : ReaderTMonadReader<Identity.T, Int> { override val monad = identity }
    val writerT = object : WriterTMonadWriter<Identity.T, StringMonoid> {
        override val monad = identity
        override val type = StringMonoid::class.java
    }
    val eitherT = object : EitherTMonadError<Identity.T, String> { override val monad = identity }

    fun runState(v: K1<StateId, *>): Any? = StateT.narrow(v).run(3)
    fun runReader(v: K1<ReaderId, *>): Any? = ReaderT.narrow(v).run(3)
    fun runWriter(v: K1<WriterId, *>): Any? = WriterT.narrow(v).run
    fun runEither(v: K1<EitherId, *>): Any? = EitherT.narrow(v).run

    fun w(s: String) = StringMonoid(s)
    fun combine(a: StringMonoid, b: StringMonoid) = StringMonoid(a.value + b.value)
}
