package kr.ilf.kshoong

interface Destinations {
    companion object{
        const val DESTINATION_LOADING = "loading"

        const val DESTINATION_HOME = "home"
        const val DESTINATION_SYNC = "sync"
        const val DESTINATION_CALENDAR = ""
    }
}

sealed class Destination(val route: String) {
    data object Loading : Destination("loading")
    data object Home : Destination("home")
    data object Sync : Destination("sync")
    data object Calendar : Destination("calendar")
    data object Detail : Destination("detail")
}