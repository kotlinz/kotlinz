package com.github.kotlinz.kotlinz.data.freecategory

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.function.Function1

sealed class Step<A, B>: K2<Step.T, A, B> {
    class T

    object Inc: Step<Int, Int>()
    object Double: Step<Int, Int>()
    object Parse: Step<String, Int>()
    object Show: Step<Int, String>()

    companion object {
        fun <A, B> narrow(v: K1<K1<T, A>, B>): Step<A, B> = v as Step<A, B>
    }
}

object StepToFunction1: QuiverTransformation<Step.T, Function1.T, Unit> {
    @Suppress("UNCHECKED_CAST")
    override fun <A, B> invoke(arrow: K2<Step.T, A, B>): K3<Function1.T, Unit, A, B> = when (Step.narrow(arrow)) {
        Step.Inc -> Function1 { a: Int -> a + 1 }
        Step.Double -> Function1 { a: Int -> a * 2 }
        Step.Parse -> Function1 { s: String -> s.toInt() }
        Step.Show -> Function1 { a: Int -> a.toString() }
    } as K3<Function1.T, Unit, A, B>
}

object StepNames: QuiverTransformation<Step.T, Function1.T, Unit> {
    @Suppress("UNCHECKED_CAST")
    override fun <A, B> invoke(arrow: K2<Step.T, A, B>): K3<Function1.T, Unit, A, B> =
        Function1 { log: List<String> -> log + Step.narrow(arrow)::class.simpleName!! } as K3<Function1.T, Unit, A, B>
}
