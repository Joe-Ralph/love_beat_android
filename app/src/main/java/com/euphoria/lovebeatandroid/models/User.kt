package com.euphoria.lovebeatandroid.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class User {
    var uuid by mutableStateOf("")
    var partnerUuid by mutableStateOf("")
}