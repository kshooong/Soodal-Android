package kr.ilf.kshoong

sealed class Destination(val route: String) {
    data object Loading : Destination("loading")
    data object Home : Destination("home")
    data object Sync : Destination("sync")
    data object Calendar : Destination("calendar")
    data object Detail : Destination("detail")
}