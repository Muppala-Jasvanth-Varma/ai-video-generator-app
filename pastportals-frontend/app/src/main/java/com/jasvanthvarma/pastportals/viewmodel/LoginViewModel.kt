package com.jasvanthvarma.pastportals.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import android.widget.Toast
import com.jasvanthvarma.pastportals.network.UserRepository

class LoginViewModel : ViewModel() {
    fun login(email: String, password: String, context: Context, onResult: (Boolean) -> Unit) {

        UserRepository.login(email, password) { success ->
            if (success) {
                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                onResult(true)
            } else {
                Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                onResult(false)
            }
        }
    }
}
