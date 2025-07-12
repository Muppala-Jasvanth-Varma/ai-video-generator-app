package com.jasvanthvarma.pastportals.viewmodel

import com.google.gson.annotations.SerializedName


// Request Data Model for Signup
data class SignupRequest(
    @SerializedName("username") val username: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)
