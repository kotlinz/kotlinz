package com.github.kotlinz.kotlinz.type.lattice

interface Lattice<A, Self: Lattice<A, Self>>: JoinSemilattice<A, Self>, MeetSemilattice<A, Self>
