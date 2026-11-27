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
import java.text.Normalizer
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var tts: TextToSpeech
    private lateinit var log: TextView

    private val hints = mapOf(
        "টিকটক" to listOf("tiktok", "musically", "trill"),
        "ইউটিউব" to listOf("youtube"),
        "ফেসবুক" to listOf("facebook.katana", "facebook"),
        "হোয়াটসঅ্যাপ" to listOf("whatsapp"),
        "ক্যামেরা" to listOf("camera"),
        "সেটিংস" to listOf("settings"),
        "মেসেঞ্জার" to listOf("messenger", "orca"),
        "গ্যালারি" to listOf("gallery", "photos"),
        "ক্রোম" to listOf("chrome")
    )

    private fun norm(x: String) = Normalizer.normalize(x, Normalizer.Form.NFC)
        .replace("\u200c", "").replace("\u200d", "").replace(" ", "").lowercase()

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
        val s = t.lowercase()
        if (s.contains("ব্যাটারি")) {
            val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
            say("ব্যাটারি ${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)} শতাংশ আছে")
            return
        }
        if (s.contains("খোল") || s.contains("খুল") || s.contains("ওপেন") || s.contains("open")) { openApp(t); return }
        say("বুঝতে পারিনি, আবার বলুন। শুনেছি: $t")
    }

    private fun openApp(t: String) {
        val n = norm(t)
        val pm = packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(main, 0)

        var hint: List<String>? = null
        for ((bn, h) in hints) if (n.contains(norm(bn))) hint = h

        var rest = n
        for (w in listOf("খোল", "খুলো", "খুল", "ওপেন", "করো", "কর", "দাও", "open")) rest = rest.replace(w, "")

        val app = apps.firstOrNull {
            val pkg = it.activityInfo.packageName.lowercase()
            val label = norm(it.loadLabel(pm).toString())
            if (hint != null) hint.any { h -> pkg.contains(h) || label.contains(h) }
            else rest.length > 1 && (label.contains(rest) || rest.contains(label))
        }
        val launch = app?.let { pm.getLaunchIntentForPackage(it.activityInfo.packageName) }
        if (launch != null) { say("খুলছি"); startActivity(launch) }
        else say("অ্যাপ পাইনি। শুনেছি: $t | অ্যাপ দেখেছি: ${apps.size}টি | হিন্ট: ${hint ?: "নেই"}")
    }
}
