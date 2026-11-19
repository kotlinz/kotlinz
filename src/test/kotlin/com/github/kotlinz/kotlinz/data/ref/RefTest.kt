package com.github.kotlinz.kotlinz.data.ref

import com.github.kotlinz.kotlinz.data.io.IO
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class RefTest : DescribeSpec({
    describe("Ref") {
        context("of") {
            it("should allocate a fresh reference on every run") {
                val alloc = Ref.of(0)
                val first = alloc.unsafeRun()
                first.set(5).unsafeRun()
                alloc.unsafeRun().get().unsafeRun() shouldBe 0
                first.get().unsafeRun() shouldBe 5
            }
        }

        context("operations") {
            val ref = Ref.unsafeOf(1)

            it("should defer effects until run") {
                val write = ref.set(42)
                ref.get().unsafeRun() shouldBe 1
                write.unsafeRun()
                ref.get().unsafeRun() shouldBe 42
            }

            it("should get and set") {
                ref.set(1).unsafeRun()
                ref.getAndSet(2).unsafeRun() shouldBe 1
                ref.get().unsafeRun() shouldBe 2
            }

            it("should update") {
                ref.set(1).unsafeRun()
                ref.update { it + 10 }.unsafeRun()
                ref.getAndUpdate { it * 2 }.unsafeRun() shouldBe 11
                ref.updateAndGet { it + 1 }.unsafeRun() shouldBe 23
            }

            it("should modify and return a derived value") {
                ref.set(3).unsafeRun()
                ref.modify { Pair(it + 1, "was $it") }.unsafeRun() shouldBe "was 3"
                ref.get().unsafeRun() shouldBe 4
            }

            it("should compose with IO") {
                val program = Ref.of(0) bind { r -> r.update { it + 1 } bind { r.update { it + 1 } } bind { r.get() } }
                program.unsafeRun() shouldBe 2
            }
        }

        context("concurrency") {
            it("should not lose updates under contention") {
                val ref = Ref.unsafeOf(0)
                val increment = ref.update { it + 1 }
                runConcurrently(8) { repeat(10_000) { increment.unsafeRun() } }
                ref.get().unsafeRun() shouldBe 80_000
            }

            it("should hand out unique values with modify") {
                val ref = Ref.unsafeOf(0)
                val next: IO<Int> = ref.modify { Pair(it + 1, it) }
                val seen = java.util.concurrent.ConcurrentLinkedQueue<Int>()
                runConcurrently(4) { repeat(1_000) { seen += next.unsafeRun() } }
                seen.toSet().size shouldBe 4_000
            }
        }
    }
})

private fun runConcurrently(threads: Int, body: () -> Unit) {
    val start = CountDownLatch(1)
    (1..threads).map { thread { start.await(); body() } }.also { start.countDown() }.forEach { it.join() }
}
