package my.com.foodie.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await

class AuthViewModel: ViewModel() {
    // Live data of user record
    private val userLiveData = MutableLiveData<User>()
    // Listen for real time changes
    private var listener: ListenerRegistration? = null

    // Remove snapshot listener when view model is destroyed
    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }

    // Return the copy of user live data
    fun getUserLiveData(): LiveData<User> {
        return userLiveData
    }

    // Return user data from live data
    fun getUser(): User? { // null means haven't login
        return userLiveData.value
    }

    //context for remember me features
    suspend fun login(ctx: Context, email: String, password: String, remember: Boolean = false): Boolean {


        val user = USER
            .whereEqualTo("emailAddress", email)
            //.whereEqualTo("password", password)
            .get()
            .await()
            .toObjects<User>()
            .firstOrNull() ?: return false // false means login failed, do not find any matches

        val hashResult = BCrypt.verifyer().verify(password.toCharArray(), user.password)
        if(hashResult.verified){
            Log.d("password is same", "yes")
            // Remove first
            listener?.remove()
            // Listen for real time changes
            listener = USER.document(user.id).addSnapshotListener { doc, _ ->
                userLiveData.value = doc?.toObject()
            }

            if (remember) {
                getPreferences(ctx)
                    .edit() // Write data into shared preferences
                    .putString("email", email) // Login Credentials
                    .putString("password", password) // Login Credentials
                    .apply()
            }
            currentUser = User(user.id,user.emailAddress,user.password,user.name,user.phoneNumber,user.role,user.birthDate,user.gender,user.userProfile)

            return true
        }else{
            Log.d("password is different", "no")
            return false
        }

    }

    fun logout(ctx: Context) {

        listener?.remove()  // No longer need to listen the changes of firestore
        userLiveData.value = null // Use to inform whether its login or logout
        currentUser = null
        // Clear shared preferences
        getPreferences(ctx).edit().clear().apply()
    }

    // Get encrypted shared preferences
    private fun getPreferences(ctx: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            "LoginUser",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            ctx,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Remember me auto login
    suspend fun loginFromPreferences(ctx: Context) {
        val pref = getPreferences(ctx)
        val email = pref.getString("email", null)
        val password = pref.getString("password", null)

        if (email != null && password != null) {
            login(ctx, email, password)
        }
    }
}