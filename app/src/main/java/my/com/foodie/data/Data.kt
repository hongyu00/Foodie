package my.com.foodie.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.com.foodie.R
import my.com.foodie.util.toBlob
import java.util.*

val USER = Firebase.firestore.collection("user")
val RESTAURANT = Firebase.firestore.collection("restaurant")
val REQUEST = Firebase.firestore.collection("request")
val REPORT = Firebase.firestore.collection("report")
val REVIEW = Firebase.firestore.collection("review")
val RESERVATION = Firebase.firestore.collection("reservation")

var currentUser: User? = null
//for add image through either camera or gallery
var cameraPhoto1: Bitmap? = null
var galleryPhoto1: Uri? = null
var cameraPhoto2: Bitmap? = null
var galleryPhoto2: Uri? = null
var cameraPhoto3: Bitmap? = null
var galleryPhoto3: Uri? = null

//for user location
var userLocation: Location? = null
var userLatitude: Double? = 0.0
var userLongitude: Double? = 0.0
var userCity: String? = "Kuala Lumpur"
var userAddress: String? = ""
var geolocation : GeoLocation? = null

//to claim restaurant
var claimRestaurantID: String? = ""
var userToOwnerAddRestaurant = false

//can delete
var returnFragment: Boolean = false
var signUpRole: String? = "User"
var userToRestaurantDetailsFragment = false
var userToRestaurantDetailsID = ""

//for enlarge image
var image: Bitmap? = null
//for random function
var listOfRestaurant: List<Restaurant>? = null
var randomRestaurant: Restaurant? = null
var randomKM: Int = 0
var randomRestaurantID: String = ""
val listOfColor = listOf("#FF0000", "#FF3F00", "#FF7F00", "#FFBF00", "#FFFF00", "#7FBF00", "#008000", "#00407F", "#0000FF", "#4000BF", "#800080", "#BF0040")

//for spinning wheel function
var pickRestaurant: MutableList<Restaurant> = ArrayList()

var isSearchRestaurant: Boolean = false


class Location(
    var location: String  = "",
    var city: String = "",
    var latitude: Double  = 0.0,
    var longitude: Double = 0.0,
)

data class User(
    @DocumentId
    var id: String = "",
    var emailAddress: String = "",
    var password: String = "",
    var name: String = "",
    var phoneNumber: String = "",
    var role: String = "",
    var birthDate: String? = "",
    var gender: String? = "",
    var userProfile: Blob? = Blob.fromBytes(ByteArray(0))

)

data class Restaurant (
    @DocumentId
    var id: String = "",
    var name: String = "",
    var location: String = "",
    var cities: String = "",
    var latitude: Double  = 0.0,
    var longitude: Double = 0.0,
    var cuisine: String = "",
    var restaurantImg: Blob? = Blob.fromBytes(ByteArray(0)),
    var restaurantSurrImg: Blob? = Blob.fromBytes(ByteArray(0)),
    var contactNo: String = "",
    var operatingHour: String = "",
    var priceRange: Int = 0,
    var description: String = "",
    var gotReservation: Boolean = false,
    var reportCount: Int = 0,
    var reviewCount: Int = 0,
    var totalRating: Float = 0.0F,
    var status: String = "",
    var dateCreated: Date = Date(),
    var viewCount: Int = 0,
    var ownerID: String = ""        //Foreign Key

){
    @get:Exclude
    var distance: Double = 0.0
    @get:Exclude
    var avgRating: Float = 0.0F
    @get:Exclude
    var reservationCount: Int = 0
}

data class Request(
    @DocumentId
    var id: String = "",
    var dateTime: Date = Date(),
    var requestType: String = "",
    var description: String = "",
    var image: Blob? = Blob.fromBytes(ByteArray(0)), //SSM
    var status: String = "",
    var rejectReason: String = "",
    var customerID: String = "",    //Foreign Key
    var restaurantID: String = "",  //Foreign Key
    var moderatorID: String = ""    //Foreign Key
){
    @get:Exclude
    var user: User = User()
    @get:Exclude
    var restaurant: Restaurant = Restaurant()
}

data class Report(
    @DocumentId
    var id: String = "",
    var dateTime: Date = Date(),
    var reportType: String = "",
    var description: String = "",
    var customerID: String = "",    //Foreign Key
    var restaurantID: String = "",  //Foreign Key
    //var moderatorID: String = ""    //Foreign Key
)

data class Review(
    @DocumentId
    var id: String = "",
    var dateTime: Date = Date(),
    var star: Float = 0.0F,
    var description: String = "",
    var isAnonymous: Boolean = false,
    var customerID: String = "",    //Foreign Key
    var restaurantID: String = "",  //Foreign Key
){
    @get:Exclude
    var user: User = User()
    @get:Exclude
    var restaurant: Restaurant = Restaurant()
}

data class Reservation(
    @DocumentId
    var id: String = "",
    var date: String = "",
    var time: String = "",
    var noOfPeople: Int = 0,
    var status: String = "",
    var rejectReason: String? = "",
    var customerID: String = "",    //Foreign Key
    var restaurantID: String = "",  //Foreign Key
){
    @get:Exclude
    var user: User = User()
    @get:Exclude
    var restaurant: Restaurant = Restaurant()
    @get:Exclude
    var hasEnded: Boolean = false
}

fun RESTORE_DATA(ctx: Context){
    USER.get().addOnSuccessListener { snap ->
        snap.documents.forEach { doc -> USER.document(doc.id).delete() }  //delete existing data and create new one in the bottom

    val users = listOf(
        User("U0000001", "user@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Chew User", "0189769192", "User",  "07-09-2000", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_1).toBlob()),
        User("U0000002", "owner@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Chew Restaurant Owner", "0123456783", "Restaurant Owner",  "17-09-2000", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_2).toBlob()),
        User("U0000003", "moderator@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Chew Moderator", "0123456781", "Moderator",  "27-09-2000", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_3).toBlob()),
        User("U0000004", "chew@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Chew Hong Yu", "0123456782", "User",  "06-09-2000", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_4).toBlob()),
        User("U0000005", "tan@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Tan Ah Li", "0129487586", "User",  "06-09-1994", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_5).toBlob()),
        User("U0000006", "haha@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Lim Xiang Ling", "0185967384", "User",  "10-10-1984", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_6).toBlob()),
        User("U0000007", "wow@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Fong Kar Hin", "0196676748", "User",  "10-01-1999", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_7).toBlob()),
        User("U0000008", "teo@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Teo Kah Kin", "0189777284", "Restaurant Owner",  "10-12-1999", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_8).toBlob()),
        User("U0000009", "minghan@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Chan Ming Han", "0122222993", "Restaurant Owner",  "22-08-2001", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_9).toBlob()),
        User("U0000010", "desmond@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Desmond Loh", "0102229934", "Restaurant Owner",  "12-12-2000", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_10).toBlob()),
        User("U0000011", "chooiyie@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Lim Chooi Yie", "0112226665", "Restaurant Owner",  "10-07-1988", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_11).toBlob()),
        User("U0000012", "boonheong@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Tan Boon Heong", "0112226666", "Restaurant Owner",  "18-08-1988", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_12).toBlob()),
        User("U0000013", "pohsam@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Lim Poh Sam", "0112226667", "Restaurant Owner",  "11-02-1988", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_13).toBlob()),
        User("U0000014", "moderator2@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "How Zheng Yan", "0169998888", "Moderator",  "09-09-1998", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_14).toBlob()),
        User("U0000015", "moderator3@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Lim Yuh Bin", "0136664444", "Moderator",  "05-05-1995", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_15).toBlob()),
        User("U0000016", "junyen@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Lim Jun Yen", "0112226666", "Restaurant Owner",  "18-08-1988", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_16).toBlob()),
        User("U0000017", "chewhongyu77@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Chew Siao Pei", "0112226667", "Restaurant Owner",  "27-03-1996", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_17).toBlob()),
        User("U0000018", "rongcheng@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Chew Rong Cheng", "0112226668", "Restaurant Owner",  "12-02-1994", "Male", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_18).toBlob()),
        User("U0000019", "paris@gmail.com", "\$2a\$12\$3OIMjLWysBIgAPkpWsG48.T0e/tu.Mz1vpH/nyY.uZCJ1eBRM28LW", "Paris Teoh", "0112226669", "Restaurant Owner",  "14-02-2000", "Female", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_profile_pic_19).toBlob()),

        )

    for (u in users){
        USER.document(u.id).set(u)
    }

    }

    RESTAURANT.get().addOnSuccessListener { snap ->
        snap.documents.forEach { doc -> RESTAURANT.document(doc.id).delete() }

        val restaurants = listOf(
            Restaurant("R0000001", "Restaurant Al Fariz Maju", "95, Jalan 1/5A, Taman Melati, Setapak, 53100, Kuala Lumpur", "Kuala Lumpur",  3.2206949504956364, 101.72561822151559, "Malaysian", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo1).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg1).toBlob(), "0123456789", "Monday-Sunday 10am-8pm", 15, "This is a very delicious restaurant that serves a variety of chinese food.", false, 2, 1,4.5F, "Active", Date(), 126,""),
            Restaurant("R0000002", "Restaurant Tan Heng", "19E, Jln Besar Kepong, Pekan Kepong, 52100 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur", "Kuala Lumpur",  3.213917314297982, 101.63521220280465, "Chinese", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo2).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg2).toBlob(), "0136663335", "Monday-Sunday 10am-8pm", 15, "This is a very delicious restaurant that serves a variety of chinese food.", true, 3, 4,16.5F, "Active", Date(), 12,"U0000002"),
            Restaurant("R0000003", "Restaurant BilaBang", "34, Jalan 3/89A, Taman Ikhsan, 56000 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur", "Kuala Lumpur",  3.111213205657576, 101.7221828297926, "Chinese", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo3).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg3).toBlob(), "0197988752", "Monday-Sunday 10am-8pm", 15, "This is a very delicious restaurant that serves a variety of chinese food.", true, 0, 0,0.0F, "Inactive", Date(), 0, "U0000008"),
            Restaurant("R0000004", "Restaurant Lau Heong", "Sentul Perdana, 43A, 0-9, Jalan 3/48a, Bandar Baru Sentul, 51000 Kuala Lumpur, Federal Territory of Kuala Lumpur, Malaysia", "Kuala Lumpur",  3.1818074999999997, 101.69714669999999, "Chinese", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo4).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg4).toBlob(), "0179273647", "Everyday from 10am to 10pm", 25, "This store sell a variety of dishes. Waiting you to explore more.", false, 0, 0,0.0F, "Active", Date(), 78,""),
            Restaurant("R0000005", "Mishaltit", "19-17, Jalan Melati Utama 2, Seksyen 3 Wangsa Maju, 53100 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur", "Kuala Lumpur",  3.2223648656312247, 101.7286420955438, "Malay", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo5).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg5).toBlob(), "0186699523", "Closed on monday. Tuesday to Sunday from 10am to 10pm.", 8, "This is a malay stores that sells many food.", false, 0, 1,4.0F, "Active", Date(), 256,""),
            Restaurant("R0000006", "Hidden paradise", "Lorong Masria 4, Taman Bunga Raya, 53100 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur", "Kuala Lumpur",  3.2136834292814243, 101.72819361090615, "Western", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo6).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg6).toBlob(), "0187666749", "Open daily from 10am to 1pm. And reopen at 5pm to 10pm.", 12, "Come and explore hidden paradise. You will be amaze on the food for sure. Western, Chinese, Malaysian food all served here.", true, 0, 1,3.5F, "Active", Date(), 66,"U0000019"),
            Restaurant("R0000007", "Thai In Mookata Setapak", "3, Jalan 14/27b, Desa Setapak, 53300 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur", "Kuala Lumpur",  3.206675772316141, 101.73363983626236, "Thai", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo7).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg7).toBlob(), "0341317128", "Open daily from 5pm to 12am", 30, "这件店是专卖烤肉店。欢迎你们的到来。请多多支持我们。谢谢。", false, 0, 1,5.0F, "Active", Date(), 99,""),
            Restaurant("R0000008", "Kari Kepala Ikan Rampai", "No. 20 & 22, Jalan 42/26, Taman Sri Rampai, 53300 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur", "Kuala Lumpur",  3.195548192164097, 101.73297234238883, "Chinese", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo8).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg8).toBlob(), "0340232708", "Thursday closed. Other day open from 11.30am to 3pm, 5.30pm to 8pm.", 20, "We sell chinese food. Noodles are sold here as well.", false, 0, 1,4.0F, "Active", Date(), 209,""),
            Restaurant("R0000009", "Absolute Thai", "Jalan Tanpa Nama, 69000 Genting Highlands, Pahang, Malaysia", "Genting Highlands",  3.403119300999702, 101.7803706228733, "Thai", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo9).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg9).toBlob(), "0340232709", "Open daily from 11.30am to 9.30pm", 40, "Thai food are served here. Delicious!", false, 0, 1,4.5F, "Active", Date(), 231,""),
            Restaurant("R0000010", "Restoran Foon Lock", "Kampung Bukit Tinggi,Lebuhraya Karak, Kampung Bukit Tinggi, 28750 Bentong, Pahang, Malaysia", "Bentong",  3.349881518956803, 101.82122770696878, "Chinese", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo10).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg10).toBlob(), "0340232710", "monday off. Tuesday to Sunday 10am to 10pm", 20, "this is a nice restaurant.", false, 0, 1,4.0F, "Active", Date(), 0,""),
            Restaurant("R0000011", "Restoran Kolam Ikan", "Jln Marak, Kampung Bukit Tinggi, 28750 Bentong, Pahang, Malaysia", "Bentong",  3.356562172980727, 101.81615632027388, "Chinese", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo11).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg11).toBlob(), "0340232711", "everyday 10am to 8pm.", 25, "What fish you can think of, we will definitely have it here.", false, 0, 1,2.0F, "Active", Date(), 157,""),
            Restaurant("R0000012", "DAR Hadramawt", "15, Jalan 2/14, Dataran Templer, 68100 Batu Caves, Selangor, Malaysia", "Batu Caves",  3.256883861284034, 101.65819365531208, "Asian", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo12).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg12).toBlob(), "0340232712", "everyday 10am to 8pm.", 10, "We sell mamak food.", false, 0, 1,1.0F, "Active", Date(), 85,""),
            Restaurant("R0000013", "Kaki Corner", "24, Jalan Siput Akek, Taman Billion, 56000 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur, Malaysia", "Kuala Lumpur",  3.0981152926669577, 101.7371542006731, "Cafe", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo13).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg13).toBlob(), "0340232716", "Open from 12pm to 8pm.", 15, "Cozy cafe. Chill environment. Perfect for gatherings with friends, families or colleague.", false, 0, 0,0.0F, "Active", Date(), 6,""),
            Restaurant("R0000014", "Good Taste Restaurant", "1, Jalan Malinja 2, Taman Bunga Raya, 53000 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur", "Kuala Lumpur",  3.212413968433689, 101.7289942789927, "Chinese", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo14).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg14).toBlob(), "0340232717", "Open from 8am to 8pm.", 7, "Food court that sells alot of food such as chicken rice, mixed rice, noodles, western and many more.", false, 1, 0,0.0F, "Active", Date(), 564,""),
            Restaurant("R0000015", "Magnifico Cafe - Setapak", "17, Jalan Malinja 2, Taman Bunga Raya, 53100 Kuala Lumpur, Wilayah Persekutuan Kuala Lumpur", "Kuala Lumpur",  3.2127292526783706, 101.72850944428464, "Vegetarian", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantlogo15).toBlob(), BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_restaurantsurrimg15).toBlob(), "01155567670", "Open daily from 10am to 9pm.", 12, "We sell delicious vegetarian food.", false, 0, 0,0.0F, "Active", Date(), 111,""),
            Restaurant("R0000016", "Mali's Corner", "Gerai Makan Radziah, Jalan Malinja 2, Taman Bunga Raya, 53300, Kuala Lumpur, Wilayah Persekutuan, Taman Bunga Raya, 53100, Federal Territory of Kuala Lumpur", "Kuala Lumpur",  3.212245179858625, 101.72890815703798, "Malaysian", null, null, "01155567671", "Open 24 hours.", 5, "", false, 0, 0,0.0F, "Inactive", Date(), 0,""),
            Restaurant("R0000017", "null restaurant", "53100, Jalan Kampung, Warisan Cityview, 53100 Kuala Lumpur, Federal Territory of Kuala Lumpur", "Kuala Lumpur",  3.234215572601992, 101.74981326184665, "Malaysian", null, null, "", "", 1000, "", false, 0, 0,0.0F, "Inactive", Date(), 0,""),
            Restaurant("R0000018", "Restoran BRJ Bistro Corner", "Seksyen 2 Wangsa Maju, 53300 Kuala Lumpur, Federal Territory of Kuala Lumpur", "Kuala Lumpur",  3.207111360980087, 101.73500183297789, "Malay", null, null, "01155567679", "Open 24 hours.", 10, "", false, 0, 0,0.0F, "Inactive", Date(), 0,""),


            )
        for (r in restaurants){
            RESTAURANT.document(r.id).set(r)
        }
    }

    REQUEST.get().addOnSuccessListener { snap ->
        snap.documents.forEach { doc -> REQUEST.document(doc.id).delete() }

        val requests = listOf(
            Request("RQ0000001", Date(), "Add Restaurant(User)", "", null, "Approved", "", "U0000001", "R0000001", "U0000003"),
            Request("RQ0000002", Date(), "Claim Business", "I am the owner of this restaurant. Please approve thanks.", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_ssm_1).toBlob(), "Approved", "", "U0000002", "R0000002", "U0000003"),
            Request("RQ0000003", Date(), "Add Restaurant(Owner)", "I have attached my SSM Cert. Please review it.", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_ssm_2).toBlob(), "Approved", "", "U0000008", "R0000003", "U0000003"),
            Request("RQ0000004", Date(), "Add Restaurant(User)", "", null, "Pending", "", "U0000004", "R0000016", ""),
            Request("RQ0000005", Date(), "Claim Business", "", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_ssm_3).toBlob(), "Pending", "", "U0000009", "R0000004", ""),
            Request("RQ0000006", Date(), "Add Restaurant(Owner)", "", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_ssm_4).toBlob(), "Pending", "", "U0000010", "R0000005", ""),
            Request("RQ0000007", Date(), "Add Restaurant(User)", "", null, "Rejected", "Details provided are wrong and no restaurant is located at there.", "U0000005", "R0000017", "U0000003"),
            Request("RQ0000008", Date(), "Claim Business", "", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_ssm_5).toBlob(), "Rejected", "The SSM Certificate image is wrong", "U0000011", "R0000006", "U0000003"),
            Request("RQ0000009", Date(), "Add Restaurant(Owner)", "", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_ssm_5).toBlob(), "Rejected", "The SSM Certificate image is wrong", "U0000012", "R0000018", "U0000003"),
            Request("RQ0000010", Date(), "Claim Business", "", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_ssm_7).toBlob(), "Pending", "", "U0000016", "R0000004", ""),
            Request("RQ0000011", Date(), "Claim Business", "", BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_ssm_8).toBlob(), "Pending", "", "U0000017", "R0000004", ""),
            )

        for (r in requests){
            REQUEST.document(r.id).set(r)
        }

    }

    REPORT.get().addOnSuccessListener { snap ->
        snap.documents.forEach { doc -> REPORT.document(doc.id).delete() }

        val reports = listOf(
            Report("RP0000001", Date(), "This restaurant has wrong information", "The restaurant name is Restoran not Restaurant", "U0000001", "R0000001"),
            Report("RP0000002", Date(), "This restaurant has wrong information", "The operating hour is wrong. It should be open 24/7.", "U0000004", "R0000001"),
            Report("RP0000003", Date(), "This restaurant no longer operated", "This restaurant has stopped operating in 10/10/2021.", "U0000005", "R0000014"),
            Report("RP0000004", Date(), "This restaurant did not accept reservation at all", "I have make reservation for so long already yet they did not accept or reject my reservation", "U0000006", "R0000002"),
            Report("RP0000005", Date(), "This restaurant did not accept reservation at all", "This restaurant did not approve my reservation at all!", "U0000007", "R0000002"),
            Report("RP0000006", Date(), "This restaurant did not accept reservation at all", "No update after i make a reservation.", "U0000001", "R0000002"),
        )

        for (r in reports){
            REPORT.document(r.id).set(r)
        }

    }

    REVIEW.get().addOnSuccessListener { snap ->
        snap.documents.forEach { doc -> REVIEW.document(doc.id).delete() }

        val reviews = listOf(
            Review("RV0000001", Date(), 4.5F, "The fried rice is so delicious i like it", false, "U0000001", "R0000001"),
            Review("RV0000002", Date(), 5.0F, "I have been eating this restaurant for 4 years! Delicious!", false, "U0000004", "R0000002"),
            Review("RV0000003", Date(), 4.5F, "Recommend the Kam Heong Fried Rice. A must try dish for them.", false, "U0000005", "R0000002"),
            Review("RV0000004", Date(), 3.0F, "The staff attitude is so bad at there but the food is nice.", true, "U0000006", "R0000002"),
            Review("RV0000005", Date(), 4.0F, "", false, "U0000007", "R0000002"),
            Review("RV0000006", Date(), 4.0F, "Environment is nice. I like the food as well", true, "U0000001", "R0000005"),
            Review("RV0000007", Date(), 3.5F, "The food can be better", false, "U0000001", "R0000006"),
            Review("RV0000008", Date(), 5.0F, "", false, "U0000001", "R0000007"),
            Review("RV0000009", Date(), 4.0F, "", false, "U0000001", "R0000008"),
            Review("RV0000010", Date(), 4.5F, "The food is nice but is expensive.", false, "U0000001", "R0000009"),
            Review("RV0000011", Date(), 4.0F, "Good food.", false, "U0000001", "R0000010"),
            Review("RV0000012", Date(), 2.0F, "Bad food.", false, "U0000001", "R0000011"),
            Review("RV0000013", Date(), 1.0F, "The food is terrible!", false, "U0000001", "R0000012"),

        )

        for (r in reviews){
            REVIEW.document(r.id).set(r)
        }

    }

    RESERVATION.get().addOnSuccessListener { snap ->
        snap.documents.forEach { doc -> RESERVATION.document(doc.id).delete() }

        val reservations = listOf(
            Reservation("RS0000001", "14-09-2021", "12:00", 2, "Approved","", "U0000001", "R0000002"),
            Reservation("RS0000002", "17-10-2021", "16:00", 3, "Rejected", "Restaurant is fully booked","U0000001", "R0000002"),
            Reservation("RS0000003", "11-11-2021", "15:30", 4, "Approved", "","U0000001", "R0000002"),
            Reservation("RS0000004", "06-06-2021", "10:00", 6, "Approved", "","U0000004", "R0000002"),
            Reservation("RS0000005", "07-09-2021", "20:30", 3, "Rejected", "Restaurant is fully booked","U0000004", "R0000002"),
            Reservation("RS0000006", "17-10-2021", "22:00", 2, "Approved", "","U0000005", "R0000002"),
            Reservation("RS0000007", "25-11-2021", "16:00", 6, "Pending", "","U0000001", "R0000002"),
            Reservation("RS0000008", "25-07-2021", "19:00", 3, "Approved", "","U0000004", "R0000003"),
            Reservation("RS0000009", "25-09-2021", "20:15", 4, "Approved", "","U0000005", "R0000003"),
            Reservation("RS0000010", "25-09-2021", "20:15", 4, "Approved", "","U0000005", "R0000006"),

            )

        for (r in reservations){
            RESERVATION.document(r.id).set(r)
        }

    }
}

