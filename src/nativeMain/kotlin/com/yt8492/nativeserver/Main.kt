package com.yt8492.nativeserver

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.AF_INET
import platform.posix.AI_PASSIVE
import platform.posix.NI_MAXHOST
import platform.posix.NI_MAXSERV
import platform.posix.NI_NUMERICHOST
import platform.posix.NI_NUMERICSERV
import platform.posix.SOCK_STREAM
import platform.posix.SOMAXCONN
import platform.posix.accept
import platform.posix.addrinfo
import platform.posix.bind
import platform.posix.close
import platform.posix.errno
import platform.posix.freeaddrinfo
import platform.posix.getaddrinfo
import platform.posix.getnameinfo
import platform.posix.listen
import platform.posix.memset
import platform.posix.recv
import platform.posix.send
import platform.posix.sockaddr
import platform.posix.sockaddr_storage
import platform.posix.socket

const val MAX_BUF_SIZE = 1024

fun main() {
    val sock = getSocket("8080")
    doService(sock)
    close(sock)
}

fun getSocket(port: String): Int {
    return memScoped {
        val hints = alloc<addrinfo>()
        var eCode: Int
        memset(hints.ptr, 0, sizeOf<addrinfo>().toULong())
        hints.ai_family = AF_INET
        hints.ai_socktype = SOCK_STREAM
        hints.ai_flags = AI_PASSIVE
        val res = alloc<CPointerVar<addrinfo>>()
        eCode = getaddrinfo(null, port, hints.ptr, res.ptr)
        if (eCode != 0) {
            throw IllegalStateException("getaddrinfo failed. error $eCode")
        }
        val hBuf = allocArray<ByteVar>(NI_MAXHOST)
        val sBuf = allocArray<ByteVar>(NI_MAXSERV)
        eCode = getnameinfo(res.pointed?.ai_addr, res.pointed?.ai_addrlen ?: 0u, hBuf, NI_MAXHOST, sBuf, NI_MAXSERV, NI_NUMERICHOST or NI_NUMERICSERV)
        if (eCode != 0) {
            freeaddrinfo(res.value)
            throw IllegalStateException("getnameinfo failed. error $eCode")
        }
        println("port: ${sBuf.toKString()}")
        println("host: ${hBuf.toKString()}")
        val sock = socket(res.pointed?.ai_family ?: AF_INET, res.pointed?.ai_socktype ?: SOCK_STREAM, res.pointed?.ai_protocol ?: AI_PASSIVE)
        if (sock < 0) {
            freeaddrinfo(res.value)
            throw IllegalStateException("get socket failed.")
        }
        eCode = bind(sock, res.pointed?.ai_addr, res.pointed?.ai_addrlen ?: 0u)
        if (eCode < 0) {
            close(sock)
            throw IllegalStateException("bind failed. error $eCode")
        }
        eCode = listen(sock, SOMAXCONN)
        if (eCode < 0) {
            close(sock)
            throw IllegalStateException("listen failed. error $eCode")
        }
        sock
    }
}

fun doService(socket: Int) {
    memScoped {
        val hBuf = allocArray<ByteVar>(NI_MAXHOST)
        val sBuf = allocArray<ByteVar>(NI_MAXSERV)
        val fromSockAddr = alloc<sockaddr_storage>()
        val addrLen = alloc<UIntVar>()
        while (true) {
            addrLen.value = sizeOf<sockaddr_storage>().toUInt()
            val acceptSocket = accept(socket, fromSockAddr.reinterpret<sockaddr>().ptr, addrLen.ptr)
            if (acceptSocket == -1) {
                throw IllegalStateException("accept failed")
            }
            getnameinfo(fromSockAddr.reinterpret<sockaddr>().ptr, addrLen.value, hBuf, NI_MAXHOST, sBuf, NI_MAXSERV, NI_NUMERICHOST or NI_NUMERICSERV)
            println("port: ${sBuf.toKString()}")
            println("host: ${hBuf.toKString()}")
            echoBack(acceptSocket)
            close(acceptSocket)
        }
    }
}

fun echoBack(socket: Int) {
    memScoped {
        val buf = ByteArray(MAX_BUF_SIZE)
        buf.usePinned { pinned ->
            while (true) {
                val receiveLength = recv(socket, pinned.addressOf(0), buf.size.convert(), 0).toInt()
                when (receiveLength) {
                    0 -> break
                    -1 -> throw IllegalStateException("errno: $errno")
                }
                val sendLength = send(socket, pinned.addressOf(0), receiveLength.convert(), 0).toInt()
                if (sendLength != receiveLength) {
                    throw IllegalStateException("send failed")
                }
            }
        }
    }
}
