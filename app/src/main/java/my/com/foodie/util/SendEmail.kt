package my.com.foodie.util

// Dependencies:
// =============
// implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0'
// implementation 'com.sun.mail:android-mail:1.6.6'
// implementation 'com.sun.mail:android-activation:1.6.6'

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.MimeMessage

class SendEmail(
    private var to     : String  = "",
    private var subject: String  = "",
    private var content: String  = "",
    private var isHtml : Boolean = false,
) {
    // --- UPDATE THE FOLLOWINGS -------------------------------------------------------------------

    private val username = "foodieisbest@gmail.com"
    private val password = "foodie123"
    private val personal = "Foodie"

    private val host = "smtp.gmail.com"
    private val port = "587"

    // ---------------------------------------------------------------------------------------------

    private val from = "$personal<$username>"
    private var message: MimeMessage? = null

    fun to(to: String): SendEmail {
        this.to = to
        return this
    }

    fun subject(subject: String): SendEmail {
        this.subject = subject
        return this
    }

    fun content(content: String): SendEmail {
        this.content = content
        return this
    }

    fun isHtml(isHtml: Boolean = true): SendEmail {
        this.isHtml = isHtml
        return this
    }

    private fun getMessage(): MimeMessage {
        if (message == null) {
            val prop = Properties()
            prop["mail.smtp.host"] = host
            prop["mail.smtp.port"] = port
            prop["mail.smtp.starttls.enable"] = "true"
            prop["mail.smtp.auth"] = "true"

            val auth = object : Authenticator() {
                override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
            }

            val sess = Session.getDefaultInstance(prop, auth)

            message = MimeMessage(sess)
        }

        return message!!
    }

    fun send(callback: () -> Unit = {}) {
        val type = if (isHtml) "text/html;charset=utf-8" else "text/plain;charset=utf-8"

        val msg = getMessage()
        msg.setFrom(from)
        msg.setRecipients(Message.RecipientType.TO, to)
        msg.subject = subject
        msg.setContent(content, type)

        CoroutineScope(Dispatchers.IO).launch {
            // NOTE: Use try-catch-finally block to silent runtime error
            Transport.send(msg)
            withContext(Dispatchers.Main) { callback() }
        }
    }
}