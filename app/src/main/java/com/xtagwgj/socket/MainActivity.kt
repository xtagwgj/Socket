package com.xtagwgj.socket

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.xtagwgj.socket.udp.ISocketListener
import com.xtagwgj.socket.udp.UdpConnectionInfo
import com.xtagwgj.socket.udp.UdpSocketProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), ISocketListener {

    private val mAdapter by lazy {
        LogAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mAdapter
        }

        UdpSocketProvider.initSocket(
                UdpConnectionInfo("255.255.255.255", 11556, 11557, 0),
                this
        )

        findViewById<View>(R.id.bt_sendUdp).setOnClickListener {
            sendInfoByUdp()
        }
    }

    private fun sendInfoByUdp() {
        val json = JSONObject()
        json.put("name", "Michael")
        UdpSocketProvider.send(json.toString())
    }

    override fun onSocketSendResult(sendSuccess: Boolean) {
        SLog.d("udp", "信息发送结果:$sendSuccess")
        runOnUiThread {
            mAdapter.addNew("【NOTE】>>>> 信息发送结果:$sendSuccess")
        }
    }

    override fun onSocketReceiveResult(receiveInfo: String) {
        runOnUiThread {
            mAdapter.addNew("【NOTE】>>>> 收到服务端消息:$receiveInfo")
        }
    }

    override fun onStop() {
        onSocketClose()
        super.onStop()
    }

    override fun onSocketClose() {
        UdpSocketProvider.closeSocket()
        SLog.e("udp", "socket 已被关闭")
    }

    class LogAdapter : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

        private val datas = mutableListOf<String>()

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val info: TextView = itemView.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                    android.R.layout.simple_list_item_1,
                    parent,
                    false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: LogAdapter.ViewHolder, position: Int) {
            holder.info.text = datas[position]
        }

        fun addNew(newInfo: String) {
            datas.add(0, newInfo)
            notifyItemInserted(0)
        }
    }
}