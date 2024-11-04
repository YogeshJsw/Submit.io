package com.yogeshj.autoform

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.autoform.authentication.User
import com.yogeshj.autoform.authentication.admin.AdminFirstScreenActivity
import com.yogeshj.autoform.authentication.uploadForm.UploadFormLoginActivity
import com.yogeshj.autoform.authentication.user.UserLoginActivity
import com.yogeshj.autoform.databinding.ActivityFirstScreenBinding
import com.yogeshj.autoform.uploadForm.FormDetailsActivity
import com.yogeshj.autoform.user.HomeScreenActivity
//Admin@123


//notification
//Add integration

class FirstScreenActivity : AppCompatActivity() {
    private lateinit var binding:ActivityFirstScreenBinding
    private lateinit var dialog:Dialog

    companion object{
        lateinit var auth: FirebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityFirstScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.apply { alpha = 0f; translationY = -30f }
        binding.loginAsUserCard.apply { alpha = 0f; translationX = -30f }
        binding.loginAsUploadformCard.apply { alpha = 0f; translationX = -30f }
        binding.title.animate().alpha(1f).translationY(0f).setDuration(1000).start()
        binding.loginAsUserCard.animate().alpha(1f).translationX(0f).setDuration(1000).setStartDelay(300).start()
//        binding.loginAsAdmin.animate().alpha(1f).translationX(0f).setDuration(700).setStartDelay(500).start()
        binding.loginAsUploadformCard.animate().alpha(1f).translationX(0f).setDuration(1000).setStartDelay(600).start()


        initLoadingDialog()

        showLoading()

        auth=FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if(currentUser!=null)
        {
            val db = FirebaseDatabase.getInstance().getReference("Users")
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(User::class.java)!!
                            if (curr.uid == auth.currentUser!!.uid) {
                                if(curr.email=="yogesh.jaiswal21b@iiitg.ac.in"){
                                    startActivity(Intent(this@FirstScreenActivity,AdminFirstScreenActivity::class.java))
                                }
                                else
                                    startActivity(Intent(this@FirstScreenActivity, HomeScreenActivity::class.java))
                                finish()
                                break
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            val db2 = FirebaseDatabase.getInstance().getReference("UploadFormUsers")
            db2.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(User::class.java)!!
                            if (curr.uid == auth.currentUser!!.uid) {
                                startActivity(Intent(this@FirstScreenActivity, FormDetailsActivity::class.java))
                                finish()
                                break
                            }
                        }
                    }
                    hideLoading()
                }

                override fun onCancelled(error: DatabaseError) {
                    hideLoading()
                }
            })

        }
        else
            hideLoading()


        binding.loginAsUserCard.setOnClickListener {
            startActivity(Intent(this@FirstScreenActivity,UserLoginActivity::class.java))
            finish()
        }

//        binding.loginAsAdmin.setOnClickListener {
//            startActivity(Intent(this@FirstScreenActivity,AdminLoginActivity::class.java))
//            finish()
//        }

        binding.loginAsUploadformCard.setOnClickListener {
            startActivity(Intent(this@FirstScreenActivity,UploadFormLoginActivity::class.java))
            finish()
        }

    }

    private fun initLoadingDialog() {
        dialog = Dialog(this@FirstScreenActivity)
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