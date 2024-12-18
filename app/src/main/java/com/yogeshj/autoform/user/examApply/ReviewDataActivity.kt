package com.yogeshj.autoform.user.examApply

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.yogeshj.autoform.FirstScreenActivity
import com.yogeshj.autoform.R
import com.yogeshj.autoform.databinding.ActivityReviewDataBinding
import com.yogeshj.autoform.uploadForm.FormDetails
import com.yogeshj.autoform.user.HomeScreenActivity
import com.yogeshj.autoform.user.profile.ProfileInfoActivity
import org.json.JSONObject

class ReviewDataActivity : AppCompatActivity(),PaymentResultWithDataListener {
    private lateinit var binding:ActivityReviewDataBinding

    private lateinit var dialog:Dialog

    private val editTextMap=HashMap<String,EditText>()
    val requiredDetails=ArrayList<String>()
    var fees=0
    var email=""
    var examId=""
    var examName=""
    var examHostName=""

    lateinit var userDetails: HashMap<String,String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityReviewDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadingDialog()

        binding.navBar.apply { alpha = 0f; translationY = -30f }
        binding.profileCard.apply { alpha = 0f }
        binding.scrollView.apply { alpha = 0f; translationX = -50f }

        binding.navBar.animate().alpha(1f).translationY(0f).setDuration(1000).start()

        binding.profileCard.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800).setStartDelay(300).start()


        binding.scrollView.animate().alpha(1f).translationX(0f).setDuration(800).setStartDelay(600).start()


        showLoading()

        FirstScreenActivity.auth = FirebaseAuth.getInstance()

        userDetails=HashMap()

        binding.back.setOnClickListener {
            finish()
        }

        Glide.with(this).load(R.mipmap.ic_launcher)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.profilePic)

        examName=intent.getStringExtra("heading")!!

        binding.editProfile.setOnClickListener {
            startActivity(Intent(this@ReviewDataActivity, ProfileInfoActivity::class.java))
        }



        val db = FirebaseDatabase.getInstance().getReference("UploadForm")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        for (examSnap in snap.children) {
                            if (examName == examSnap.key) {
                                val currentExam = examSnap.getValue(FormDetails::class.java)!!

                                examHostName = currentExam.examHostName!!
                                fees = currentExam.fees
                                examId = currentExam.uid!!

                                Glide.with(this@ReviewDataActivity)
                                    .load(currentExam.icon)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(binding.profilePic)




                                val appFormDetailsSnap = examSnap.child("Application Form Details")
                                for (detailSnap in appFormDetailsSnap.children) {
                                    val detail = detailSnap.getValue(String::class.java)
                                    if (detail != null) {
                                        requiredDetails.add(detail.lowercase())
                                    }
                                }
                                break
                            }
                        }
                    }
                    makeEditText()
                }
                hideLoading()
            }

            override fun onCancelled(error: DatabaseError) {
                hideLoading()
            }
        })


        val dbRef = FirebaseDatabase.getInstance().getReference("UsersInfo").child(
            FirstScreenActivity.auth.currentUser!!.uid)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userKeys=HashSet<String>()
                var dob=""
                var gender=""
                if(snapshot.exists()){

                    for(child in snapshot.children){
                        val key=child.key
                        val value=child.getValue(String::class.java)

                        if(key=="name")
                            binding.name.text=value
                        if(key=="dob")
                            dob=value!!
                        if(key=="gender")
                            gender=value!!
                        if(key=="phone")
                            binding.phone.text=value
                        if(key=="email")
                            binding.email.text=value
                        if(key=="profilePic") {
                            Glide.with(this@ReviewDataActivity)
                                .load(value)
                                .placeholder(R.drawable.user_icon)
                                .error(R.drawable.user_icon)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.profilePic)
                        }
                        Log.d("FirebaseFields", "Key: $key, Value: $value")

                        if(key!=null && value!=null) {
                            userKeys.add(key)
                            userDetails.put(key,value)
                        }

                        editTextMap[key]?.setText(value)
                        editTextMap[key]?.isEnabled=false

                    }
                    binding.dobGender.text = "$dob $gender"
                    requiredDetails.removeAll(userKeys)

                    if(requiredDetails.size>0)
                    {
                        for(key in requiredDetails)
                            Log.d("req",key)
                        binding.moreDetailsRequiredLayout.visibility=View.VISIBLE
                        binding.moreDetailsRequiredLayout.animate().alpha(1f).translationX(0f).setDuration(700).setStartDelay(500).start()
                        binding.payBtn.visibility=View.GONE
//                        binding.saveBtn.visibility=View.VISIBLE
                    }
                    else
                    {
                        Toast.makeText(this@ReviewDataActivity,"Make sure your profile is fully updated!",Toast.LENGTH_LONG).show()
                        binding.payBtn.visibility=View.VISIBLE
                        binding.payBtn.animate().alpha(1f).setDuration(700).setStartDelay(800).start()
                        binding.moreDetailsRequiredLayout.visibility=View.GONE
//                        binding.saveBtn.visibility=View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error retrieving data", error.toException())
                hideLoading()
            }
        })

        binding.addDataWarning.setOnClickListener {
            val intent=Intent(this@ReviewDataActivity, AdditionalDetailsRequiredActivity::class.java)
            val unfilledData= Bundle()
            for(detail in requiredDetails)
            {
                unfilledData.putString(detail.lowercase(),"")
            }
            intent.putExtras(unfilledData)
            startActivity(intent)
            finish()
        }

        Checkout.preload(applicationContext)
        val co = Checkout()
        co.setKeyID("rzp_test_0DG18Nx16j11Ph")

        binding.payBtn.setOnClickListener {
            initPayment()
        }
    }

    private fun extractFormData(): Map<String, String> {
        return editTextMap.mapValues { entry ->
            entry.value.text.toString()
        }
    }


    private fun makeEditText() {
        for (detail in requiredDetails) {
            val textInputLayout = com.google.android.material.textfield.TextInputLayout(this@ReviewDataActivity)
            textInputLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textInputLayout.hint = detail.capitalize()

            val editText = com.google.android.material.textfield.TextInputEditText(textInputLayout.context)

            editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            editText.textSize = 16f

            textInputLayout.addView(editText)
            binding.editTextContainer.addView(textInputLayout)
            editTextMap.put(detail.lowercase(),editText)
        }
    }


    private fun initPayment() {
        val co = Checkout()
        try {
            val options = JSONObject()
            options.put("name", "Submit.io")
            options.put("description", "Fees for ${binding.name.text}: $fees")
            options.put("image", "https://media.tradly.app/images/marketplace/34521/razor_pay_icon-ICtywSbN.png")
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            options.put("amount", (fees * 100).toString())

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val preFill = JSONObject()
            preFill.put("email", email)
            preFill.put("contact","7982582284")
//            options.put("prefill",preFill)
            co.open(this@ReviewDataActivity as Activity?, options)
        } catch (e: Exception) {
            Toast.makeText(this@ReviewDataActivity,"Error intializing",Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

     override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        showLoading()
        val userId = FirstScreenActivity.auth.currentUser?.uid
        val paymentRefUser = FirebaseDatabase.getInstance().getReference("Payment")
        val paymentId = paymentRefUser.push().key

         val applicationFormData = extractFormData()


         val paymentData = PaymentDataModel(userId,examId,fees,examName,examHostName,"success",email)



        val paymentRefHis = FirebaseDatabase.getInstance().getReference("PaymentHistory")
        paymentRefHis.child(paymentId!!).setValue(paymentData)
         paymentRefHis.child(paymentId).child("ApplicationFormDetails").setValue(applicationFormData)

        paymentRefUser.child(paymentId).setValue(paymentData)
            .addOnSuccessListener {
                paymentRefUser.child(paymentId).child("ApplicationFormDetails").setValue(applicationFormData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@ReviewDataActivity,
                            "Payment Success",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        hideLoading()
                        val intent = Intent(this@ReviewDataActivity, HomeScreenActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this@ReviewDataActivity, "Error saving form details: ${it.message}", Toast.LENGTH_LONG).show()
                        hideLoading()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@ReviewDataActivity, "Error: ${e.message}", Toast.LENGTH_LONG)
                    .show()
                hideLoading()
            }
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Toast.makeText(this@ReviewDataActivity, "Error: $p2", Toast.LENGTH_LONG).show()
    }


    private fun initLoadingDialog() {
        dialog = Dialog(this@ReviewDataActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun showLoading() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun hideLoading() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }


}