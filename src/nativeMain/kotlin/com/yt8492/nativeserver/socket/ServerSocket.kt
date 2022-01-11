package com.yt8492.nativeserver.socket

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
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
import platform.posix.freeaddrinfo
import platform.posix.getaddrinfo
import platform.posix.close as cClose
import platform.posix.getnameinfo
import platform.posix.listen
import platform.posix.memset
import platform.posix.sockaddr
import platform.posix.sockaddr_storage
import platform.posix.socket

class ServerSocket(
    val port: Int,
) {

    private val socketDescriptor: Int = memScoped {
        val hints = alloc<addrinfo>()
        var eCode: Int
        memset(hints.ptr, 0, sizeOf<addrinfo>().toULong())
        hints.ai_family = AF_INET
        hints.ai_socktype = SOCK_STREAM
        hints.ai_flags = AI_PASSIVE
        val res = allocPointerTo<addrinfo>()
        eCode = getaddrinfo(null, port.toString(), hints.ptr, res.ptr)
        if (eCode != 0) {
            throw SocketException("getaddrinfo failed. error $eCode")
        }
        val hBuf = allocArray<ByteVar>(NI_MAXHOST)
        val sBuf = allocArray<ByteVar>(NI_MAXSERV)
        eCode = getnameinfo(res.pointed?.ai_addr, res.pointed?.ai_addrlen ?: 0u, hBuf, NI_MAXHOST, sBuf, NI_MAXSERV, NI_NUMERICHOST or NI_NUMERICSERV)
        if (eCode != 0) {
            freeaddrinfo(res.value)
            throw SocketException("getnameinfo failed. error $eCode")
        }
        val sock = socket(res.pointed?.ai_family ?: AF_INET, res.pointed?.ai_socktype ?: SOCK_STREAM, res.pointed?.ai_protocol ?: AI_PASSIVE)
        if (sock < 0) {
            freeaddrinfo(res.value)
            throw SocketException("get socket failed.")
        }
        eCode = bind(sock, res.pointed?.ai_addr, res.pointed?.ai_addrlen ?: 0u)
        if (eCode < 0) {
            cClose(sock)
            throw SocketException("bind failed. error $eCode")
        }
        eCode = listen(sock, SOMAXCONN)
        if (eCode < 0) {
            cClose(sock)
            throw SocketException("listen failed. error $eCode")
        }
        sock
    }

    fun accept(): Socket {
        return memScoped {
            val hBuf = allocArray<ByteVar>(NI_MAXHOST)
            val sBuf = allocArray<ByteVar>(NI_MAXSERV)
            val fromSockAddr = alloc<sockaddr_storage>()
            val addrLen = alloc<UIntVar>()
            addrLen.value = sizeOf<sockaddr_storage>().toUInt()
            val acceptSocket = accept(
                socketDescriptor,
                fromSockAddr.reinterpret<sockaddr>().ptr,
                addrLen.ptr,
            )
            if (acceptSocket == -1) {
                throw SocketException("accept failed")
            }
            getnameinfo(
                fromSockAddr.reinterpret<sockaddr>().ptr,
                addrLen.value,
                hBuf,
                NI_MAXHOST,
                sBuf,
                NI_MAXSERV,
                NI_NUMERICHOST or NI_NUMERICSERV,
            )
            val host = hBuf.toKString()
            val port = sBuf.toKString().toInt()
            Socket(
                host,
                port,
                acceptSocket,
            )
        }
    }

    fun close() {
        cClose(socketDescriptor)
    }
}
