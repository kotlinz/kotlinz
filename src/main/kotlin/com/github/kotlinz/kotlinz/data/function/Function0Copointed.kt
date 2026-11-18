package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [Function0]; [extract] runs the thunk. */
interface Function0Copointed: Copointed<Function0.T> {
  override fun <A> extract(v: K1<Function0.T, A>): A = Function0.narrow(v).run()
}
