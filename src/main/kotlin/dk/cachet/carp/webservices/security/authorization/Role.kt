package dk.cachet.carp.webservices.security.authorization

enum class Role {
    UNKNOWN,
    PARTICIPANT,
    RESEARCHER_ASSISTANT,
    RESEARCHER,
    CARP_ADMIN,
    SYSTEM_ADMIN,
    ;

    companion object {
        fun fromString(value: String): Role {
            return entries.find { it.name.equals(value.removePrefix("ROLE_"), ignoreCase = true) } ?: UNKNOWN
        }
    }

    fun prettyPrint(): String =
        when (this) {
            UNKNOWN -> throw IllegalArgumentException("Unknown role: $this")
            PARTICIPANT -> "Participant"
            RESEARCHER -> "Researcher"
            RESEARCHER_ASSISTANT -> "Researcher Assistant"
            CARP_ADMIN -> "CARP Admin"
            SYSTEM_ADMIN -> "System Admin"
        }
}
