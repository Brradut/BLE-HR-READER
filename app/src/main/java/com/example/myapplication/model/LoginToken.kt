package com.example.myapplication.model

import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

class LoginToken(var user: String, var password: String) {
    init{
    user = MessageDigest.getInstance("SHA-256").digest(user.toByteArray(UTF_8)).joinToString (separator = ""){ byte -> "%02x".format(byte) }
    password = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(UTF_8)).joinToString (separator = ""){ byte -> "%02x".format(byte) }
    }
}