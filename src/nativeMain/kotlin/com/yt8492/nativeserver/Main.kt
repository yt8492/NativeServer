package com.yt8492.nativeserver

import com.yt8492.nativeserver.http.Headers
import com.yt8492.nativeserver.http.Server
import com.yt8492.nativeserver.http.response.ServerResponse
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.usePinned
import platform.posix.SIGINT
import platform.posix.STDOUT_FILENO
import platform.posix.signal
import platform.posix.write

var server: Server? = null

fun main() {
    shutdownHook()
    server = Server()
    val server = server ?: return
    server.get("/hoge/:fuga") { request ->
        val fuga = request.pathParameters.require("fuga")
        val body = """{"message": "fuga: $fuga"}""".encodeToByteArray()
        return@get ServerResponse(
            statusCode = 200,
            reasonPhrase = "OK",
            body = body,
        )
    }
    server.listen(8080)
}

fun shutdownHook() {
    signal(SIGINT, staticCFunction<Int, Unit> {
        server?.close()
        "server stop\n".usePinned { pinned ->
            write(STDOUT_FILENO, pinned.addressOf(0), 24)
        }
    })
}
