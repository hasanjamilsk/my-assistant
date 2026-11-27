package com.hasan.assistant

import android.app.Activity
import android.content.Intent
import android.os.BatteryManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var tts: TextToSpeech
    private lateinit var log: TextView

    private val names = mapOf(
        "টিকটক" to "tiktok", "ইউটিউব" to "youtube", "ফেসবুক" to "facebook",
        "হোয়াটসঅ্যাপ" to "whatsapp", "ক্যামেরা" to "camera", "সেটিংস" to "settings",
        "মেসেঞ্জার" to "messenger", "গ্যালারি" to "gallery", "ক্রোম" to "chrome"
    )

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        tts = TextToSpeech(this) { s ->
            if (s == TextToSpeech.SUCCESS) tts.language = Locale("bn", "BD")
        }
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40, 80, 40, 40)
        val btn = Button(this)
        btn.text = "🎤 বলুন"
        btn.textSize = 22f
        btn.setOnClickListener { listen() }
        log = TextView(this)
        log.textSize = 18f
        log.text = "মাইক চেপে বলুন, যেমন: টিকটক খোল"
        root.addView(btn)
        root.addView(log)
        setContentView(root)
    }

    private fun say(t: String) {
        log.text = t
        tts.speak(t, TextToSpeech.QUEUE_FLUSH, null, "x")
    }

    private fun listen() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
        try { startActivityForResult(i, 1) } catch (e: Exception) { say("ভয়েস সুবিধা পাওয়া যায়নি") }
    }

    override fun onActivityResult(r: Int, c: Int, d: Intent?) {
        super.onActivityResult(r, c, d)
        if (r == 1 && c == RESULT_OK) {
            val t = d?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: return
            handle(t)
        }
    }

    private fun handle(t: String) {
        log.text = "আপনি বললেন: $t"
        val s = t.lowercase()
        if (s.contains("ব্যাটারি")) {
            val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
            say("ব্যাটারি ${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} শতাংশ আছে")
            return
        }
        if (s.contains("খোল") || s.contains("ওপেন") || s.contains("open")) { openApp(s); return }
        say("বুঝতে পারিনি, আবার বলুন")
    }

    private fun openApp(s: String) {
        var key = s
        for ((bn, en) in names) if (s.contains(bn)) key = en
        val pm = packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val app = pm.queryIntentActivities(main, 0).firstOrNull {
            val l = it.loadLabel(pm).toString().lowercase()
            l.length > 2 && (key.contains(l) || l.contains(key))
        }
        val launch = app?.let { pm.getLaunchIntentForPackage(it.activityInfo.packageName) }
        if (launch != null) { say("খুলছি"); startActivity(launch) } else say("অ্যাপটা খুঁজে পাইনি")
    }
}
