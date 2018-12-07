package com.xtagwgj.socket

import android.util.Log

class SLog {

    companion object {

        var isDebug = true

        var tagName = "SLog"

        fun d(tag: String = tagName, msg: String) {
            if (isDebug) {
                Log.d(tag, msg)
            }
        }

        fun e(tag: String = tagName,msg: String) {
            if (isDebug) {
                Log.e(tag, msg)
            }
        }

        fun i(tag: String = tagName,msg: String) {
            if (isDebug) {
                Log.i(tag, msg)
            }
        }

        fun v(tag: String = tagName,msg: String) {
            if (isDebug) {
                Log.v(tag, msg)
            }
        }

        fun w(tag: String = tagName,msg: String) {
            if (isDebug) {
                Log.w(tag, msg)
            }
        }

    }
}