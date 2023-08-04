package dk.cachet.carp.webservices.common.environment

import java.util.*

enum class EnvironmentProfile(var value: String)
{
    LOCAL("local"),
    DEVELOPMENT("development"),
    STAGING("staging"),
    TESTING("testing"),
    PRODUCTION("production");

    companion object
    {
        fun getEnvironmentProfile(profile: String) = valueOf(profile.uppercase(Locale.getDefault()))
    }
}