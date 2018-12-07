package com.xtagwgj.socket.udp

import com.xtagwgj.socket.SLog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object UdpSocketProvider {
    private const val TAG = "UdpSocketProvider"

    private var udpConnectionInfo: UdpConnectionInfo? = null

    private var localUDPSocket: DatagramSocket? = null

    private var mSocketListener: ISocketListener? = null

    private var createDisposable: Disposable? = null

    fun initSocket(udpConnectionInfo: UdpConnectionInfo, socketListener: ISocketListener? = null) {
        this.udpConnectionInfo = udpConnectionInfo
        this.mSocketListener = socketListener

        createDisposable = Observable.just(udpConnectionInfo)
                .map {
                    // UDP本地监听端口（如果为0将表示由系统分配，否则使用指定端口）
                    val socket = DatagramSocket()
                    // 调用connect之后，每次send时DatagramPacket就不需要设计目标主机的ip和port了
                    // * 注意：connect方法一定要在DatagramSocket.receive()方法之前调用，
                    // * 不然整send数据将会被错误地阻塞。这或许是官方API的bug，也或许是调
                    // * 用规范就应该这样，但没有找到官方明确的说明
                    socket.connect(
                            InetAddress.getByName(udpConnectionInfo.mIp),
                            udpConnectionInfo.mServerPort
                    )
                    socket.reuseAddress = true
                    socket
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    localUDPSocket = it
                    SLog.d(TAG, "创建 udp 已成功完成.")

                    // 启动本地UDP监听（接收数据用的）
                    UdpDataReceiver.startUp(
                            udpConnectionInfo.mLocalPort,
                            udpConnectionInfo.mSocketTimeout,
                            mSocketListener
                    )
                }, {
                    SLog.e(TAG, "localUDPSocket创建时出错，原因是：${it.message}.")
                })
    }

    /**
     * 发送数据
     *
     * @param toServerBytes 发送内容的字节数组
     * @return 是否发送成功
     */
    private fun send(toServerBytes: ByteArray) {
        var isSendSuccess = false
        try {
            localUDPSocket?.send(DatagramPacket(toServerBytes, toServerBytes.size))
            isSendSuccess = true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mSocketListener?.onSocketSendResult(isSendSuccess)

    }

    fun send(sendInfo: String) {
        send(sendInfo.toByteArray())
    }

    fun closeSocket() {
        try {
            disposedRx(createDisposable)

            UdpDataReceiver.closeReceiver()
            localUDPSocket?.close()
        } catch (e: Exception) {
            SLog.e(TAG, e.message ?: "")
        }
    }

    private fun disposedRx(vararg mDisposable: Disposable?) {
        mDisposable.forEach {
            if (it?.isDisposed == false) {
                it.dispose()
            }
        }
    }
}