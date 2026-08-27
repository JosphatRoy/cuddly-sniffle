package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
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
            ((digits.startsWith("07") || digits.startsWith("01")) && (digits.length == 10)) -> {
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

    fun sendDispatchAlert(context: Context, order: Order, etaMinutes: Int, driverNotes: String = "") {
        val formattedPhone = formatPhoneNumber(order.customerPhone)
        val driverNotesSection = if (driverNotes.isNotBlank()) {
            "\n📝 *Driver Note:* $driverNotes\n"
        } else ""

        val message = """
            *DAIRY PASTURE DISPATCH* 🚛
            
            Hello *${order.customerName}*, your milk order is now in transit!
            
            📍 *Destination:* ${order.address}
            🥛 *Quantity:* ${order.liters} Liters
            ⏱️ *Estimated Arrival:* $etaMinutes minutes
            🛣️ *Route:* ${order.routeName}
            $driverNotesSection
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

    private fun isNumberOnWhatsApp(context: Context, phone: String): Boolean {
        // We check if the number has a WhatsApp profile in the contacts database
        // This requires READ_CONTACTS permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        
        // Extract last 9 digits for more robust matching (e.g. 712345678)
        val searchSuffix = if (phone.length >= 9) phone.substring(phone.length - 9) else phone
        
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.CONTACT_ID)
        val selection = "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        // WhatsApp uses this specific mimetype for its contact sync profiles
        val selectionArgs = arrayOf("vnd.android.cursor.item/vnd.com.whatsapp.profile", "%$searchSuffix")

        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                cursor.count > 0
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun sendViaWhatsAppOrSms(context: Context, phone: String, message: String) {
        val isWhatsAppInstalled = try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (_: Exception) {
            try {
                context.packageManager.getPackageInfo("com.whatsapp.w4b", 0)
                true
            } catch (_: Exception) {
                false
            }
        }

        // WhatsApp requirement: installed, formatted correctly (254...), and has a WA account
        // If we can't verify WA account (no permission or not in contacts), we fallback to SMS 
        // to ensure the message is delivered reliably.
        if (isWhatsAppInstalled && phone.startsWith("254") && phone.length == 12 && isNumberOnWhatsApp(context, phone)) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=$phone&text=" + URLEncoder.encode(message, "UTF-8")
                intent.data = url.toUri()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (_: Exception) {
                sendSms(context, phone, message)
            }
        } else {
            sendSms(context, phone, message)
        }
    }

    private fun sendSms(context: Context, phone: String, message: String) {
        // Automatic background SMS requires SEND_SMS permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                if (smsManager != null) {
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
                    return // Exit if sent successfully
                }
            } catch (e: Exception) {
                // Log error or ignore to fall back to intent
            }
        }

        // Fallback to manual SMS Intent
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:$phone".toUri()
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(smsIntent)
    }
}
