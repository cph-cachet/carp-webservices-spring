package dk.cachet.carp.webservices.common.extensions

import java.util.*


fun String.toSnakeCase(): String =
    this.replace( Regex( "([a-z])([A-Z]+)" ), "$1_$2" )
        .replace( Regex( "([A-Z])([A-Z][a-z])" ), "$1_$2" )
        .lowercase(Locale.getDefault())