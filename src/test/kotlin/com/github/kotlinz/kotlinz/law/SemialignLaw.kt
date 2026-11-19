package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.align.Align
import com.github.kotlinz.kotlinz.type.align.Semialign
import com.github.kotlinz.kotlinz.type.align.Zip
import io.kotest.matchers.booleans.shouldBeTrue

class SemialignLaw<T>(val semialign: Semialign<T>) {
    fun <A> idempotency(x: K1<T, A>): Boolean =
            semialign.align(x, x) == semialign.fmap({ a: A -> Ior.both(a, a) }, x)

    fun <A> commutativity(x: K1<T, A>, y: K1<T, A>): Boolean =
            semialign.align(x, y) == semialign.fmap({ i: Ior<A, A> -> swap(i) }, semialign.align(y, x))

    fun <A> associativity(x: K1<T, A>, y: K1<T, A>, z: K1<T, A>): Boolean =
            semialign.align(x, semialign.align(y, z)) == semialign.fmap({ i: Ior<Ior<A, A>, A> -> assoc(i) }, semialign.align(semialign.align(x, y), z))

    fun <A> functoriality(x: K1<T, A>, y: K1<T, A>, f: (A) -> A, g: (A) -> A): Boolean =
            semialign.align(semialign.fmap(f, x), semialign.fmap(g, y)) == semialign.fmap({ i: Ior<A, A> -> i.bimap(f, g) }, semialign.align(x, y))

    fun <A> alignWithConsistency(x: K1<T, A>, y: K1<T, A>): Boolean {
        val f = { i: Ior<A, A> -> i.toString() }
        return semialign.alignWith(f, x, y) == semialign.fmap(f, semialign.align(x, y))
    }

    fun <A> assertSatisfyingSemialignLaw(x: K1<T, A>, y: K1<T, A>, z: K1<T, A>, f: (A) -> A, g: (A) -> A) {
        idempotency(x).shouldBeTrue()
        commutativity(x, y).shouldBeTrue()
        associativity(x, y, z).shouldBeTrue()
        functoriality(x, y, f, g).shouldBeTrue()
        alignWithConsistency(x, y).shouldBeTrue()
    }
}

class AlignLaw<T>(val align: Align<T>) {
    fun <A> rightIdentity(x: K1<T, A>): Boolean = align.align(x, align.nil<A>()) == align.fmap({ a: A -> Ior.left<A, A>(a) }, x)
    fun <A> leftIdentity(x: K1<T, A>): Boolean = align.align(align.nil<A>(), x) == align.fmap({ a: A -> Ior.right<A, A>(a) }, x)

    fun <A> assertSatisfyingAlignLaw(x: K1<T, A>) {
        rightIdentity(x).shouldBeTrue()
        leftIdentity(x).shouldBeTrue()
    }
}

class ZipLaw<T>(val zip: Zip<T>) {
    fun <A> idempotency(x: K1<T, A>): Boolean = zip.zip(x, x) == zip.fmap({ a: A -> Pair(a, a) }, x)

    fun <A> commutativity(x: K1<T, A>, y: K1<T, A>): Boolean =
            zip.zip(x, y) == zip.fmap({ p: Pair<A, A> -> Pair(p.second, p.first) }, zip.zip(y, x))

    fun <A> associativity(x: K1<T, A>, y: K1<T, A>, z: K1<T, A>): Boolean =
            zip.zip(x, zip.zip(y, z)) == zip.fmap({ p: Pair<Pair<A, A>, A> -> Pair(p.first.first, Pair(p.first.second, p.second)) }, zip.zip(zip.zip(x, y), z))

    fun <A> absorption(x: K1<T, A>, y: K1<T, A>): Boolean = zip.fmap({ p: Pair<A, Ior<A, A>> -> p.first }, zip.zip(x, zip.align(x, y))) == x

    fun <A> zipWithConsistency(x: K1<T, A>, y: K1<T, A>): Boolean {
        val f = { a: A, b: A -> "$a-$b" }
        return zip.zipWith(f, x, y) == zip.fmap({ p: Pair<A, A> -> f(p.first, p.second) }, zip.zip(x, y))
    }

    fun <A> assertSatisfyingZipLaw(x: K1<T, A>, y: K1<T, A>, z: K1<T, A>) {
        idempotency(x).shouldBeTrue()
        commutativity(x, y).shouldBeTrue()
        associativity(x, y, z).shouldBeTrue()
        absorption(x, y).shouldBeTrue()
        zipWithConsistency(x, y).shouldBeTrue()
    }
}

private fun <A, B> swap(i: Ior<A, B>): Ior<B, A> = when (i) {
    is Ior.Left -> Ior.right(i.value)
    is Ior.Right -> Ior.left(i.value)
    is Ior.Both -> Ior.both(i.right, i.left)
}

private fun <A, B, C> assoc(i: Ior<Ior<A, B>, C>): Ior<A, Ior<B, C>> = when (i) {
    is Ior.Right -> Ior.right(Ior.right(i.value))
    is Ior.Left -> assocLeft(i.value, null)
    is Ior.Both -> assocLeft(i.left, i)
}

private fun <A, B, C> assocLeft(inner: Ior<A, B>, outer: Ior.Both<Ior<A, B>, C>?): Ior<A, Ior<B, C>> = when (inner) {
    is Ior.Left -> if (outer == null) Ior.left(inner.value) else Ior.both(inner.value, Ior.right(outer.right))
    is Ior.Right -> Ior.right(if (outer == null) Ior.left(inner.value) else Ior.both(inner.value, outer.right))
    is Ior.Both -> Ior.both(inner.left, if (outer == null) Ior.left(inner.right) else Ior.both(inner.right, outer.right))
}
