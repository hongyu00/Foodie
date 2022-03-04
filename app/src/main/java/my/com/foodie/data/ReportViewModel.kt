package my.com.foodie.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.core.Repo
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.com.foodie.util.generateID

class ReportViewModel: ViewModel() {

    private val reports = MutableLiveData<List<Report>>()
    private var rep = listOf<Report>()
    private var lastID = ""

    private var restID = ""
    init{
        viewModelScope.launch {
            REPORT.addSnapshotListener { snap, _ -> if(snap == null) return@addSnapshotListener
                rep = snap.toObjects()

                lastID = if(rep.isEmpty()){
                    "RP0000000"
                }else{
                    rep.last().id
                }
                updateResult()
            }
        }
    }

    private fun updateResult() {
        var list = rep

        list = list.filter {
                r -> r.restaurantID == restID
        }

        reports.value = list
    }

    fun get(id: String): Report?{
        return reports.value?.find { rp -> rp.id == id }
    }
    fun set(rp: Report){
        REPORT.document(rp.id).set(rp)
    }

    fun delete(id: String){
        REPORT.document(id).delete()
    }

    fun filterByRestaurant(restaurantID: String) {
        restID = restaurantID
        updateResult()
    }

    suspend fun getReportByUserAndRestaurant(userID: String, restaurantID: String): Report?{
        val report = REPORT.whereEqualTo("customerID", userID).whereEqualTo( "restaurantID", restaurantID).get().await().toObjects<Report>().firstOrNull()
        return report
    }

    fun getAll() = reports

    fun generateID(): String = generateID(lastID)



}
