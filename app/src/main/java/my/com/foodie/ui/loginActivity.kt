package my.com.foodie.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.AuthViewModel
import my.com.foodie.data.User
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.ActivityLoginBinding

@Suppress("DEPRECATION")
class loginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    var progressDialog: ProgressDialog? = null
    private val authVM: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkInternetConnection()
        checkUserRememberMe()
    }

    private fun checkUserRememberMe(){
        // Retrieve remember me user
        lifecycleScope.launch { authVM.loginFromPreferences(this@loginActivity) }

        authVM.getUserLiveData().observe(this) { user ->
            if (user != null) { // When login credentials match will go here (else will go to Login Fragment to further validate the conditions)
                currentUser = User(user.id,user.emailAddress,user.password,user.name,user.phoneNumber,user.role,user.birthDate,user.gender,user.userProfile)
                progressDialog = ProgressDialog(this@loginActivity)
                progressDialog?.setTitle("Loading")
                progressDialog?.setMessage("Please wait a moment")
                progressDialog?.show()
                val role = user.role

                if (role == getString(R.string.user)) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else if(role == getString(R.string.restaurant_owner)) {
                    val intent = Intent(this, MainBusinessActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }else {
                    val intent = Intent(this, MainModeratorActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }

    @SuppressLint("MissingPermission")
    fun checkInternetConnection(): Boolean {
        val conMgr = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conMgr.activeNetworkInfo
        if (netInfo == null || !netInfo.isConnected || !netInfo.isAvailable) {
            AlertDialog.Builder(this@loginActivity)
                .setTitle("Wifi Disconnected")
                .setMessage("Seems like you are offline now.\nPlease turn on your Wi-Fi and come back again." )
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(getString(R.string.ok)
                ) { _, _ -> finish() }.show()
            return false
        }
        return true
    }
}