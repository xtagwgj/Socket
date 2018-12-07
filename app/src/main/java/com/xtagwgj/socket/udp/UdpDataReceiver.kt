package com.xtagwgj.socket.udp

import com.xtagwgj.socket.SLog
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

object UdpDataReceiver {
    private const val TAG = "UdpDataReceiver"

    private val dataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    private var localUdpSocket: DatagramSocket? = null

    private var mReceiveThread: Thread? = null

    fun startUp(mLocalPort: Int, timeout: Int, socketListener: ISocketListener?) {
        mReceiveThread = Thread(Runnable {
            try {
                localUdpSocket = DatagramSocket(mLocalPort)
                SLog.d(TAG, "【NOTE】>>>>>> 本地UDP端口侦听中，端口=$mLocalPort...")
                udpListeningImpl(timeout, socketListener)
            } catch (e: Exception) {
                if (e is SocketTimeoutException) {
                    socketListener?.onSocketClose()
                    SLog.e(TAG, "【NOTE】>>>>>> 本地UDP监听停止了(socket被关闭了?),${e.message}")
                } else {
                    e.printStackTrace()
                }
            }
        })

        mReceiveThread?.start()
    }


    private fun udpListeningImpl(timeout: Int, socketListener: ISocketListener?) {
        val data = ByteArray(512)
        //接收数据报的包
        val packet = DatagramPacket(data, data.size)

        while (true) {
            if (mReceiveThread?.isInterrupted == true) {
                SLog.w(TAG, "【NOTE】>>>>>> udp监听接口已停止")
                return
            }

            if (localUdpSocket?.isClosed == true) {
                SLog.w(TAG, "【NOTE】>>>>>> udp监听接口已关闭")
                Thread.sleep(1000)
                continue
            }

            SLog.w(TAG, "【NOTE】>>>>>> 阻塞直到收到数据")

            //超时时间
            localUdpSocket?.soTimeout = timeout
            //阻塞直到收到数据
            localUdpSocket?.receive(packet)

            //解析服务端发过来的数据
            val pFromServer = String(
                    packet.data,
                    0,
                    packet.length,
                    Charset.forName("UTF-8")
            )
            SLog.d(TAG, "【NOTE】>>>>>> 收到服务端的消息(${dataFormat.format(Date())})：$pFromServer")
            socketListener?.onSocketReceiveResult(pFromServer)
        }
    }

    fun closeReceiver() {
        mReceiveThread?.interrupt()
        localUdpSocket?.close()
    }

}