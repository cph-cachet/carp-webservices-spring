package dk.cachet.carp.webservices.common.email.util

import dk.cachet.carp.webservices.common.email.domain.EmailType
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailTemplateUtil(private val templateEngine: TemplateEngine)
{
    companion object
    {
        const val INLINE_CACHET_LOGO_ID = "logo"
        const val INLINE_CPH_LOGO_ID = "cph"
        const val INLINE_DTU_LOGO_ID = "dtu"
        const val INLINE_H_LOGO_ID = "h"
        const val INLINE_KU_LOGO_ID = "ku"
        const val INLINE_STUDY_INVITATION_ID = "inv"
        const val INLINE_STUDY_DESCRIPTION_ID = "desc"
        const val NOTIFICATION_EMAIL = "notification_email"
        const val PNG_CONTENT_TYPE = "image/png"
    }

    fun inviteAccount(invitation: String, description: String?, emailType: EmailType): String
    {
        val context = getContextWithDefaults()
        context.setVariable(INLINE_STUDY_INVITATION_ID, invitation)
        context.setVariable(INLINE_STUDY_DESCRIPTION_ID, description)

        return when (emailType)
        {
            EmailType.INVITE_NEW_ACCOUNT -> templateEngine.process("account/invite-new-account.html", context)
            EmailType.INVITE_EXISTING_ACCOUNT -> templateEngine.process("account/invite-existing-account.html", context)
        }
    }

    fun sendNotificationEmail(message: String?): String
    {
        return notificationTemplate(message)
    }

    private fun notificationTemplate(message: String?): String
    {
        val context = getContextWithDefaults()
        context.setVariable(NOTIFICATION_EMAIL, message)

        return templateEngine.process("alert/notification.html", context)
    }

    private fun getContextWithDefaults(): Context
    {
        val context = Context()
        context.setVariable(INLINE_CACHET_LOGO_ID, INLINE_CACHET_LOGO_ID)
        context.setVariable(INLINE_CPH_LOGO_ID, INLINE_CPH_LOGO_ID)
        context.setVariable(INLINE_DTU_LOGO_ID, INLINE_DTU_LOGO_ID)
        context.setVariable(INLINE_H_LOGO_ID, INLINE_H_LOGO_ID)
        context.setVariable(INLINE_KU_LOGO_ID, INLINE_KU_LOGO_ID)
        return context
    }
}