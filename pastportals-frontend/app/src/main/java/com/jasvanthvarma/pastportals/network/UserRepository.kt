package com.jasvanthvarma.pastportals.network

import com.jasvanthvarma.pastportals.viewmodel.LoginRequest
import com.jasvanthvarma.pastportals.viewmodel.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UserRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.136.184:5000/") // Replace with your backend URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        val call = apiService.login(LoginRequest(email, password))
        call.enqueue(object : Callback<LoginResponse> {  //  Fix: Use Callback<LoginResponse>
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    callback(true)
                } else {
                    callback(false)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                callback(false)
            }
        })
    }
}

private fun <T> Call<T>.enqueue(callback: Callback<LoginResponse>) {

}
