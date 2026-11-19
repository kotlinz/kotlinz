package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.IxMonad
import io.kotest.matchers.shouldBe

class IxMonadLaw<T>(val monad: IxMonad<T>, val run: (K3<T, *, *, *>) -> Any?) {
    fun <I, J, A, B> leftIdentity(a: A, f: (A) -> K3<T, I, J, B>) {
        run(monad.ibind(f, monad.ipure<I, A>(a))) shouldBe run(f(a))
    }

    fun <I, J, A> rightIdentity(v: K3<T, I, J, A>) {
        run(monad.ibind({ a: A -> monad.ipure<J, A>(a) }, v)) shouldBe run(v)
    }

    fun <I, J, K, L, A, B, C> associativity(v: K3<T, I, J, A>, f: (A) -> K3<T, J, K, B>, g: (B) -> K3<T, K, L, C>) {
        run(monad.ibind(g, monad.ibind(f, v))) shouldBe run(monad.ibind({ a: A -> monad.ibind(g, f(a)) }, v))
    }
}
