package com.example.myapplication.utils

import com.example.myapplication.model.LoginToken
import java.io.FileInputStream
import java.util.*

class PropertyUtils {
    companion object {
        val ins = this.javaClass.classLoader.getResourceAsStream("config.properties")
        val prop = Properties()

        init {
            prop.load(ins)
        }
        fun getLoginToken():LoginToken{
            return LoginToken(prop.getProperty("user"), prop.getProperty("password"))
        }
        fun getRESTEndpoint():String{
            return prop.getProperty("url")
        }
    }
}