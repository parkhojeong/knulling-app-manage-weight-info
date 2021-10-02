package com.example.recordweightinfo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.content.Context

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.*

import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*
import android.widget.TextView
import android.widget.Toast
import android.app.AlertDialog


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var database: DatabaseReference
    lateinit var mAdapter: ListView_Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Firebase.database.reference
        loadTodayWeightInfo()
        addWeitghInfo()
        asyncTodayWeightInfo()
    }

    fun addWeitghInfo() {
        val weightAddButton: Button = findViewById(R.id.weightAddButton)
        weightAddButton.setOnClickListener {
            val typeText = findViewById<EditText>(R.id.weightTypeAddText).text.toString()
            val countText = findViewById<EditText>(R.id.weightCountAddText).text.toString()
            val setText = findViewById<EditText>(R.id.weightSetAddText).text.toString()
            val restTimeText = findViewById<EditText>(R.id.weightRestTimeAddText).text.toString()

            if (typeText.isEmpty() || countText.isEmpty() || setText.isEmpty()) {
                Toast.makeText(this.applicationContext, "빈 칸을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentDateRef =
                database.child("weightInfo").child("bob").child(getCurrentDateString())

            val count = countText.toInt()
            val set = setText.toInt()
            val restTime = if (restTimeText == "") 0 else restTimeText.toInt()
            currentDateRef.push().setValue(
                mapOf(
                    "count" to count,
                    "set" to set,
                    "restTime" to restTime,
                    "type" to typeText
                )
            )
        }
    }

    fun loadTodayWeightInfo() {
        val listView: ListView = findViewById(R.id.listview_list)

        database.child("weightInfo").get().addOnSuccessListener {
            Log.d(TAG, "[OnSuccessListener]")
            val items = mutableListOf<ListView_Item>()

            it.child("bob").child(getCurrentDateString()).children.forEach { data ->
                val id: String = data.key as String
                val set: Number = data.child("set").value?.toString()?.toInt() ?: 0
                val count: Number = data.child("count").value?.toString()?.toInt() ?: 0
                val type: String = data.child("type").value?.toString() ?: ""
                val restTime: Number = data.child("restTime").value?.toString()?.toInt() ?: 0

                items.add(ListView_Item(id, count, set, type, restTime))
            }

            mAdapter = ListView_Adapter(this, items, database, ::delteItem, ::editItem)
            listView.setAdapter(mAdapter)
        }.addOnFailureListener {
            Log.d(TAG, "[OnFailureListener] Error getting data $it")
        }
    }

    fun editItem(id: String, type: String, count: Int, set: Int, restTime: Int) {
        val listViewItem = ListView_Item(id, count, set, type, restTime)

        database
            .child(
                "weightInfo"
            ).child("bob") // TODO: apply memeber id
            .child(getCurrentDateString())
            .child(id)
            .setValue(listViewItem)
            .addOnSuccessListener {
                Toast.makeText(this@MainActivity, "완료", Toast.LENGTH_SHORT).show()
            }
    }

    fun delteItem(id: String) {
        // TODO: apply memeber id ("bob")
        database.child(
            "weightInfo"
        ).child("bob").child(getCurrentDateString()).child(id).removeValue()
    }

    fun asyncTodayWeightInfo() {
        database.child("weightInfo").child("bob")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange")
                    val listView: ListView = findViewById(R.id.listview_list)
                    val items = mutableListOf<ListView_Item>()

                    dataSnapshot.child(getCurrentDateString()).children.forEach { data ->
                        val id: String = data.key as String
                        val set: Number = data.child("set").value?.toString()?.toInt() ?: 0
                        val count: Number = data.child("count").value?.toString()?.toInt() ?: 0
                        val type: String = data.child("type").value?.toString() ?: ""
                        val restTime: Number =
                            data.child("restTime").value?.toString()?.toInt() ?: 0

                        items.add(ListView_Item(id, count, set, type, restTime))
                    }

                    mAdapter = ListView_Adapter(
                        applicationContext,
                        items,
                        database,
                        ::delteItem,
                        ::editItem
                    )

                    listView.setAdapter(mAdapter)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Failed to read value.", error.toException())
                }
            })
    }

    inner class ListView_Adapter(
        context: Context,
        items: List<ListView_Item>?,
        databaseReference: DatabaseReference,
        deleteItem: (id: String) -> Unit,
        editItem: (id: String, type: String, count: Int, set: Int, restTime: Int) -> Unit
    ) :
        BaseAdapter() {
        var items: List<ListView_Item>? = null
        var context: Context
        var database: DatabaseReference
        var deleteItem: (id: String) -> Unit
        var editItem: (id: String, type: String, count: Int, set: Int, restTime: Int) -> Unit

        init {
            this.items = items
            this.context = context
            this.database = databaseReference
            this.deleteItem = deleteItem
            this.editItem = editItem
        }

        override fun getCount(): Int {
            return items?.size ?: 0
        }

        override fun getItem(position: Int): ListView_Item {
            return items!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        // Adapter.getView() 해당위치 뷰 반환 함수
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = layoutInflater.inflate(R.layout.listview_item, parent, false)

            val index: TextView = view.findViewById(R.id.listitem_index)
            val count: TextView = view.findViewById(R.id.listitem_count)
            val set: TextView = view.findViewById(R.id.listitem_set)
            val type: TextView = view.findViewById(R.id.listitem_type)
            val restTime: TextView = view.findViewById(R.id.listitem_resttime)

            val item = items!![position]
            index.text = (position + 1).toString() // 해당위치 +1 설정, 배열순으로 0부터 시작
            type.text = item.type
            count.text = item.count.toString()
            set.text = item.set.toString()
            restTime.text = item.restTime.toString()

            view.findViewById<Button>(R.id.listitem_deleteButton).setOnClickListener {
                deleteItem(item.id)
            }

            view.findViewById<Button>(R.id.listitem_editButton).setOnClickListener {

                val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
                val dialogView = layoutInflater.inflate(R.layout.dialog, parent, false)
                val dialogWeightInfoTypeText =
                    dialogView.findViewById<EditText>(R.id.dialogWeightInfoTypeText)
                val dialogWeightInfoCountText =
                    dialogView.findViewById<EditText>(R.id.dialogWeightInfoCountText)
                val dialogWeightInfoSetText =
                    dialogView.findViewById<EditText>(R.id.dialogWeightInfoSetText)
                val dialogWeightInfoRestTimeText =
                    dialogView.findViewById<EditText>(R.id.dialogWeightInfoRestTimeText)

                dialogWeightInfoTypeText.setText(type.text)
                dialogWeightInfoCountText.setText(count.text)
                dialogWeightInfoSetText.setText(set.text)
                dialogWeightInfoRestTimeText.setText(restTime.text)

                alertDialogBuilder.setView(dialogView)
                    .setPositiveButton("확인") { dialogInterface, i ->
                        val type = dialogWeightInfoTypeText.text.toString()
                        val set = dialogWeightInfoSetText.text.toString().toInt()
                        val count = dialogWeightInfoCountText.text.toString().toInt()
                        val restTime = dialogWeightInfoRestTimeText.text.toString().toInt()

                        editItem(item.id, type, count, set, restTime)

                    }
                    .setNegativeButton("취소") { dialogInterface, i ->
                        Toast.makeText(context, "취소", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }

            return view
        }
    }

}


class ListView_Item(
    val id: String,
    val count: Number,
    val set: Number,
    val type: String,
    val restTime: Number?
) {
}

fun getCurrentDateString(): String {
    return SimpleDateFormat("y-MM-d").format(Date()).toString()
}