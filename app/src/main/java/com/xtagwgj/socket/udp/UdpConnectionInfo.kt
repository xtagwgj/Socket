package com.xtagwgj.socket.udp

data class UdpConnectionInfo(
        val mIp: String = "255.255.255.255",
        val mServerPort: Int,
        val mLocalPort: Int,
        val mSocketTimeout: Int = 0
)