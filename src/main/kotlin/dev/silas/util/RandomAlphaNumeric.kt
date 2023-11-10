package dev.silas.util

import kotlin.random.Random

object RandomAlphaNumeric : (Int) -> String {

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    override fun invoke(length: Int) = (1..length)
        .map {
            Random.nextInt(0, charPool.size).let { charPool[it] }
        }
        .joinToString("")
}
