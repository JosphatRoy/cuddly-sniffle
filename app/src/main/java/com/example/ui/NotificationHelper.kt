package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.data.Order
import java.net.URLEncoder
import java.util.Locale

object NotificationHelper {

    private fun formatPhoneNumber(phone: String): String {
        // Remove all non-numeric characters
        val digits = phone.filter { it.isDigit() }
        
        return when {
            // If it starts with 07 or 01 (Kenyan local format)
            ((digits.startsWith("07") || digits.startsWith("01")) && digits.length == 10) -> {
                "254" + digits.substring(1)
            }
            // If it starts with 254 but no plus
            digits.startsWith("254") && digits.length == 12 -> {
                digits
            }
            // Default: return as is if we can't safely format it
            else -> digits
        }
    }

    fun sendDispatchAlert(context: Context, order: Order, etaMinutes: Int) {
        val formattedPhone = formatPhoneNumber(order.customerPhone)
        val message = """
            *DAIRY PASTURE DISPATCH* 🚛
            
            Hello *${order.customerName}*, your milk order is now in transit!
            
            📍 *Destination:* ${order.address}
            🥛 *Quantity:* ${order.liters} Liters
            ⏱️ *Estimated Arrival:* $etaMinutes minutes
            🛣️ *Route:* ${order.routeName}
            
            Our driver is on the way. Please ensure someone is available to receive the delivery.
            
            _Thank you for choosing Githunguri Dairy._
        """.trimIndent()
        
        sendViaWhatsAppOrSms(context, formattedPhone, message)
    }

    fun sendDigitalReceipt(context: Context, order: Order) {
        val formattedPhone = formatPhoneNumber(order.customerPhone)
        val total = order.liters * order.pricePerLiter
        val date = java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm", Locale.getDefault()).format(java.util.Date())
        
        val message = """
            *DAIRY PASTURE OFFICIAL RECEIPT* 🧾
            
            *Date:* $date
            *Customer:* ${order.customerName}
            --------------------------
            🥛 *Volume:* ${order.liters}L
            💰 *Rate:* KSh ${"%.2f".format(order.pricePerLiter)}/L
            ✅ *Total Paid:* *KSh ${"%.2f".format(total)}*
            --------------------------
            💳 *Method:* ${order.paymentMethod}
            📊 *Status:* COMPLETED
            
            _Thank you for your business! Your support helps our local farmers._
        """.trimIndent()
        
        sendViaWhatsAppOrSms(context, formattedPhone, message)
    }

    private fun sendViaWhatsAppOrSms(context: Context, phone: String, message: String) {
        val isWhatsAppAvailable = try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (_: Exception) {
            try {
                context.packageManager.getPackageInfo("com.whatsapp.w4b", 0) // Check Business version
                true
            } catch (_: Exception) {
                false
            }
        }

        // If WhatsApp is available and the number looks like a valid international format (e.g. 254...)
        if (isWhatsAppAvailable && phone.startsWith("254") && phone.length == 12) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=$phone&text=" + URLEncoder.encode(message, "UTF-8")
                intent.data = url.toUri()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Last resort fallback to SMS if intent fails
                sendSms(context, phone, message)
            }
        } else {
            // Go directly to SMS if WhatsApp is missing or number isn't perfectly formatted for it
            sendSms(context, phone, message)
        }
    }

    private fun sendSms(context: Context, phone: String, message: String) {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:$phone".toUri()
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(smsIntent)
    }
}
