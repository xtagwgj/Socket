package com.xtagwgj.socket.udp

interface ISocketListener {
    fun onSocketSendResult(sendSuccess: Boolean)

    fun onSocketReceiveResult(receiveInfo: String)

    fun onSocketClose()
}