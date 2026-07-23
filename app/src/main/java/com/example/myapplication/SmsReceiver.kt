package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.data.ScamDatabase
import com.example.myapplication.data.ScamMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val body = message.messageBody
                val sender = message.displayOriginatingAddress ?: "Unknown"
                
                val result = ScamDetector.analyzeMessage(body)
                if (result.isScam) {
                    val reason = result.reason ?: "Potential SASSA Scam detected!"
                    NotificationHelper.showScamNotification(context, "SASSA Shield Alert", "From $sender: $reason")
                    
                    // Save to database
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = ScamDatabase.getDatabase(context)
                            db.scamDao().insertScam(
                                ScamMessage(
                                    sender = sender,
                                    body = body,
                                    reason = reason
                                )
                            )
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }
}
