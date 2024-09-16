package com.euphoria.lovebeatandroid.models

import androidx.lifecycle.ViewModel


class User : ViewModel() {
    var uuid: String = ""
        get() = field
        set(value) {
            field = value
        }
    var partnerUuid: String = ""
        get() = field
        set(value) {
            field = value
        }

    override fun toString(): String {
        return "User(uuid='$uuid', partnerUuid='$partnerUuid')"
    }

}