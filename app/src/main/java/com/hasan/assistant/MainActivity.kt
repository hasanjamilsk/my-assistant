package com.hasan.assistant

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val t = TextView(this)
        t.text = "আমার সহকারী চালু হয়েছে ✅"
        t.textSize = 24f
        setContentView(t)
    }
}
