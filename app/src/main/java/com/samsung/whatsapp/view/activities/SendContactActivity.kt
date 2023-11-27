package com.samsung.whatsapp.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.whatsapp.ApplicationClass
import com.samsung.whatsapp.R
import com.samsung.whatsapp.databinding.ActivitySendContactBinding
import com.samsung.whatsapp.model.PhoneContact
import com.samsung.whatsapp.utils.FirebaseUtils
import com.samsung.whatsapp.utils.Utils
import com.squareup.picasso.Picasso

class SendContactActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySendContactBinding
    private lateinit var contactImage: String
    private lateinit var contactName: String
    private lateinit var contactPhone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isViewContact: Boolean = intent.getBooleanExtra("IsViewContact", false)

        val name: String? = intent.getStringExtra("name")
        val phone: String? = intent.getStringExtra("phone")
        val image: String? = intent.getStringExtra("image")
        val receiver: String? = intent.getStringExtra(getString(R.string.VISIT_USER_ID))

        setupUI(name, phone, image, isViewContact)

        if (!isViewContact) {
            binding.sendContact.setOnClickListener {
                FirebaseUtils.sendContact(PhoneContact(phone, name, image), Utils.currentUser.uid, receiver)
                finish()
            }
        } else {
            binding.sendContact.visibility = View.GONE
            binding.addContact.visibility = View.VISIBLE
            val contactId = intent.getStringExtra("contactId")
            contactId?.let {
                ApplicationClass.contactsDatabaseReference.child(it)
                    .addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            contactImage = snapshot.child("image").getValue(String::class.java).toString()
                            contactName = snapshot.child("name").getValue(String::class.java).toString()
                            contactPhone = snapshot.child("phone").getValue(String::class.java).toString()

                            binding.name.text = contactName
                            binding.phoneNumber.text = contactPhone
                            Picasso.get().load(contactImage).into(binding.image)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
            }

            binding.addContact.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@SendContactActivity)
                builder
                    .setMessage("Create a new contact or add to an existing contact?")
                    .setTitle("Save Contact")
                    .setPositiveButton("New") { _, _ ->
                        val intent = Intent(Intent.ACTION_INSERT)
                        intent.type = ContactsContract.Contacts.CONTENT_TYPE
                        intent.putExtra(ContactsContract.Intents.Insert.NAME,contactName)
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE,contactPhone)
                        this@SendContactActivity.startActivity(intent)
                    }
                    .setNegativeButton("Existing") { _, _ ->
                        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT)
                        intent.type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
                        intent.putExtra(ContactsContract.Intents.Insert.NAME,contactName)
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE,contactPhone)
                        this@SendContactActivity.startActivity(intent)
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setupUI(name: String?, phone: String?, image: String?, isViewContact: Boolean?) {
        if (isViewContact == true)
            binding.chatToolBar.mainAppBar.title = "View Contact"
        else
            binding.chatToolBar.mainAppBar.title = "Send Contact"

        binding.name.text = name
        binding.phoneNumber.text = phone
        Picasso.get().load(image).into(binding.image)
    }
}