package com.yt8492.nativeserver.stream

interface InputStream {
    fun read(buffer: ByteArray, length: Int): Int
}