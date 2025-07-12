package com.jasvanthvarma.pastportals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class SignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignupScreen()
        }
    }
}

@Composable
fun SignupScreen() {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf<String?>(null) }

    // Error states
    var usernameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(id = R.drawable.bg_image),
        contentDescription = "Background",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Sign Up", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; usernameError = false },
            label = { Text("Username") },
            isError = usernameError
        )
        if (usernameError) Text("Username cannot be empty", color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it; mobileError = false },
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = mobileError
        )
        if (mobileError) Text("Enter a valid 10-digit mobile number", color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = false },
            label = { Text("Enter your Mail ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError
        )
        if (emailError) Text("Enter a valid email", color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = false },
            label = { Text("Enter Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError
        )
        if (passwordError) Text("Password must be at least 6 characters", color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = false },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmPasswordError
        )
        if (confirmPasswordError) Text("Passwords do not match", color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                usernameError = username.isBlank()
                emailError = !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                mobileError = mobileNumber.length != 10 || mobileNumber.any { !it.isDigit() }
                passwordError = password.length < 6
                confirmPasswordError = password != confirmPassword

                if (!usernameError && !emailError && !mobileError && !passwordError && !confirmPasswordError) {
                    isLoading = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = registerUser(username, name, mobileNumber, email, password, confirmPassword, context)
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            showToast = result
                        }
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            enabled = !isLoading
        ) {
            Text(text = if (isLoading) "Registering..." else "Register & Rewrite History")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = { context.startActivity(Intent(context, DashboardActivity::class.java)) }
        ) {
            Text("Already have an account? Log in")
        }
    }

    // Show Toast if needed
    LaunchedEffect(showToast) {
        showToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it == "Signup successful!") {
                context.startActivity(Intent(context, DashboardActivity::class.java))
            }
        }
    }
}


suspend fun registerUser(username: String, name: String, mobile: String, email: String, password: String, confirmPassword: String, context: Context): String {
    val client = OkHttpClient()
    val gson = Gson()

    val jsonBody = gson.toJson(
        mapOf(
            "username" to username,
            "name" to name,
            "mobileNumber" to mobile,
            "email" to email,
            "password" to password,
            "confirmPassword" to confirmPassword  // ✅ Included confirmPassword
        )
    )

    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("http://192.168.136.184:5000/api/users/signup")  // ✅ Replace with your backend URL
        .post(requestBody)
        .build()

    return try {
        val response = client.newCall(request).execute()
        val responseText = response.body?.string()

        Log.d("SignupResponse", responseText ?: "No response")

        if (response.isSuccessful) "Signup successful!"
        else "Signup failed: $responseText"
    } catch (e: Exception) {
        Log.e("Signup", "Exception: ${e.message}")
        "Network error!"
    }
}
