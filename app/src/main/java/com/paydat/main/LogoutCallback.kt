package com.paydat.main

interface LogoutCallback {
    fun logoutUser(status: Boolean)
    fun userUpdate(status: Boolean)
}