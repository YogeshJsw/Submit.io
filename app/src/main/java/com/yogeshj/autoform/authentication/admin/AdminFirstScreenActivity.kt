package com.yogeshj.autoform.authentication.admin

import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.autoform.FirstScreenActivity
import com.yogeshj.autoform.IntroActivity
import com.yogeshj.autoform.databinding.ActivityAdminFirstScreenBinding

class AdminFirstScreenActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAdminFirstScreenBinding
    lateinit var myAdapter: UploadFormSignUpAdapter
    lateinit var dataList: ArrayList<UploadFormSignUpModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAdminFirstScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirstScreenActivity.auth= FirebaseAuth.getInstance()

        binding.logout.setOnClickListener {
            FirstScreenActivity.auth.signOut()
            TaskStackBuilder.create(this@AdminFirstScreenActivity)
                .addNextIntentWithParentStack(Intent(this@AdminFirstScreenActivity,FirstScreenActivity::class.java))
                .addNextIntent(Intent(this@AdminFirstScreenActivity, IntroActivity::class.java))
                .startActivities()
            finish()
        }

        dataList =ArrayList()
        myAdapter = UploadFormSignUpAdapter(dataList,this@AdminFirstScreenActivity)
        binding.rvRegistrationList.layoutManager=LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvRegistrationList.adapter = myAdapter
        val db = FirebaseDatabase.getInstance().getReference("UploadFormRegistrationUsers")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr=snap.getValue(UploadFormSignUpModel::class.java)
                        if (curr!=null) {
                            dataList.add(UploadFormSignUpModel(curr.websitelink,curr.instituteName,curr.headName,curr.instituteAddress,curr.instituteContact,curr.instituteMailId,curr.loginMailId,curr.key))

                        }
                    }
                    myAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}