package dk.cachet.carp.webservices.security.authorization

enum class Role
{
    UNKNOWN,
    PARTICIPANT,
    RESEARCHER,
    CARP_ADMIN,
    SYSTEM_ADMIN;

    companion object {
        fun fromString(value: String): Role {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }

    fun prettyPrint(): String = when (this) {
        UNKNOWN -> throw IllegalArgumentException("Unknown role: $this")
        PARTICIPANT -> "Participant"
        RESEARCHER -> "Researcher"
        CARP_ADMIN -> "CARP Admin"
        SYSTEM_ADMIN -> "System Admin"
    }
}