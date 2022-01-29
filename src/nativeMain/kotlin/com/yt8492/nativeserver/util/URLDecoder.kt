package com.yt8492.nativeserver.util

import kotlinx.cinterop.toKString

object URLDecoder {
    fun decode(url: String): String {
        val res = StringBuilder()
        var i = 0
        while (i < url.length) {
            var c = url[i]
            when (c) {
                '+' -> {
                    res.append(' ')
                    i++
                }
                '%' -> {
                    var buf = byteArrayOf()
                    while (i + 2 < url.length && '%' == c) {
                        val hexStr = url.substring(i + 1, i + 3)
                        buf += hexStr.toInt(16).toByte()
                        i += 3
                        if (i < url.length) {
                            c = url[i]
                        }
                    }

                    res.append(buf.toKString())
                }
                else -> {
                    res.append(c)
                    i++
                }
            }
        }
        return res.toString()
    }
}
