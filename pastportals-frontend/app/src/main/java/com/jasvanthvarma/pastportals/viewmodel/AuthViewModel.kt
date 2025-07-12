import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.jasvanthvarma.pastportals.network.RetrofitClient
import com.jasvanthvarma.pastportals.viewmodel.LoginRequest
import com.jasvanthvarma.pastportals.viewmodel.LoginResponse
import com.jasvanthvarma.pastportals.viewmodel.SignupRequest
import com.jasvanthvarma.pastportals.viewmodel.ApiResponse


class AuthViewModel : ViewModel() {

    fun signup(firstname: String, lastname: String, email: String, mobile: String, password: String, confirmPassword: String,
               onResult: (String) -> Unit)
    {
        // Ensure confirmPassword is provided
        if (confirmPassword.isEmpty()) {
            onResult("Error: Confirm Password cannot be empty")
            return
        }
        val request = SignupRequest(firstname, lastname, email, mobile, password, confirmPassword)
        Log.d("AuthViewModel", "Sending Signup Request: $firstname, $lastname, $email")
        RetrofitClient.apiService.signup(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("SIGNUP_RESPONSE", "Success: ${responseBody?.message}")
                    onResult(responseBody?.message ?: "Signup successful")
                } else {
                    Log.e("SIGNUP_ERROR", "Failed: ${response.errorBody()?.string()}")
                    onResult("Signup failed")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error: ${t.message}")
                onResult("Error: ${t.message}")
            }
        })
    }

    fun login(email: String, password: String, onResult: (String) -> Unit) {
        val request = LoginRequest(email, password)
        RetrofitClient.apiService.login(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body()?.message ?: "Login successful")
                } else {
                    onResult("Login failed")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onResult("Error: ${t.localizedMessage}")
            }
        })
    }
}
