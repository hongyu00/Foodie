package my.com.foodie.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.com.foodie.util.generateID

class UserViewModel: ViewModel() {

    private val userLiveData = MutableLiveData<List<User>>()
    private var user = listOf<User>()
    private var lastID = ""

    private var name = ""
    private var role = ""

    init {
        viewModelScope.launch {
            USER.addSnapshotListener { snap, _ -> if( snap == null) return@addSnapshotListener
                user = snap.toObjects()
                lastID = if(user.isEmpty()){
                    "U0000000"
                }else{
                    user.last().id
                }
                updateResult()
            }
        }
    }

    private fun updateResult() {
        var list = user

        list = list.filter {
                r -> r.name.contains(name, true)
                &&(role == "All" || role == r.role)
        }

        userLiveData.value = list
    }
    fun get(id: String): User?{
        return userLiveData.value?.find { r -> r.id == id }
    }

    fun getAll() = userLiveData

    fun set(user: User) {
        USER.document(user.id).set(user)
    }
    fun search(name: String) {
        this.name = name
        updateResult()
    }
    fun filterRole(role: String) {
        this.role = role
        updateResult()
    }
    fun updateDetails(id: String, name: String, phone: String, birthDate: String, gender: String) {
        USER.document(id).update("name",name,"phoneNumber",phone,"birthDate",birthDate, "gender", gender)
    }

    fun updateProfilePhoto(id: String, photo: com.google.firebase.firestore.Blob){
        USER.document(id).update("userProfile",photo)

    }

    suspend fun getUserByEmail(email: String): User? {
        return USER
            .whereEqualTo("emailAddress", email)
            .get()
            .await()
            .toObjects<User>()
            .firstOrNull()
    }

    suspend fun getUserByPhone(phone: String): User? {
        return USER
            .whereEqualTo("phoneNumber", phone)
            .get()
            .await()
            .toObjects<User>()
            .firstOrNull()
    }

    fun updatePassword(id: String, password: String){
        val passHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        USER.document(id).update("password",passHash)
    }

    fun generateID(): String = generateID(lastID)

}