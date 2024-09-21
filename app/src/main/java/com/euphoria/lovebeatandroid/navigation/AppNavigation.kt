package com.euphoria.lovebeatandroid.navigation

enum class Screen(val route: String) {
    MAIN("main"),
    PAIRING("pairing"),
    VIBRATION("vibration"),
    SENDER_SCREEN("sender_screen"),
    RECEIVER_SCREEN("receiver_screen"),
    CONSENT_SCREEN("consent/{deviceAddress}"),
    SUCCESS_SCREEN("success/{partnerUuid}")
}

sealed class NavigationItem(val route: String) {
    object Main : NavigationItem(Screen.MAIN.name)
    object Pairing : NavigationItem(Screen.PAIRING.name)
    object SenderScreen : NavigationItem(Screen.SENDER_SCREEN.name)
    object ReceiverScreen : NavigationItem(Screen.RECEIVER_SCREEN.name)
    object Vibration : NavigationItem(Screen.VIBRATION.name)
    object Consent : NavigationItem(Screen.CONSENT_SCREEN.name)
    object Success : NavigationItem(Screen.SUCCESS_SCREEN.name)

}