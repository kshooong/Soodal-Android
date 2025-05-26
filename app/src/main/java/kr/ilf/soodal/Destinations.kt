package kr.ilf.soodal

sealed class Destination(val route: String) {
    data object Loading : Destination("loading")
    data object Sync : Destination("sync")
    data object Calendar : Destination("calendar")
    data object Settings : Destination("settings")
}