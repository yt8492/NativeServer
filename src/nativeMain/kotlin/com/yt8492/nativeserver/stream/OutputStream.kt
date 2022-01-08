package com.yt8492.nativeserver.stream

interface OutputStream {
    fun write(buffer: ByteArray, length: Int): Int
    fun write(text: String): Int
}
