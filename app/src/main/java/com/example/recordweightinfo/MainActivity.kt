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

    fun addWeitghInfo(){
        val weightAddButton: Button = findViewById(R.id.weightAddButton)
        weightAddButton.setOnClickListener{
            val typeText = findViewById<EditText>(R.id.weightTypeAddText).text.toString()
            val countText = findViewById<EditText>(R.id.weightCountAddText).text.toString()
            val setText = findViewById<EditText>(R.id.weightSetAddText).text.toString()
            val restTimeText = findViewById<EditText>(R.id.weightRestTimeAddText).text.toString()

            if(typeText.isEmpty() || countText.isEmpty() || setText.isEmpty()){
                Toast.makeText(this.applicationContext, "빈 칸을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentDateRef = database.child("weightInfo").child("bob").child(getCurrentDateString())

            val count = countText.toInt()
            val set = setText.toInt()
            val restTime = if(restTimeText == "") 0 else restTimeText.toInt()
            currentDateRef.push().setValue(ListView_Item(0,count,set,typeText, restTime ))
        }
    }

    fun loadTodayWeightInfo() {
        val listView: ListView = findViewById(R.id.listview_list)

        database.child("weightInfo").get().addOnSuccessListener {
            Log.d(TAG, "[OnSuccessListener]")
            val items = mutableListOf<ListView_Item>()

            it.child("bob").child(getCurrentDateString()).children.forEach { data ->
                val set: Number = data.child("set").value?.toString()?.toInt() ?: 0
                val count: Number = data.child("count").value?.toString()?.toInt() ?: 0
                val type: String = data.child("type").value?.toString() ?: ""
                val restTime: Number = data.child("restTime").value?.toString()?.toInt() ?: 0

                items.add(ListView_Item(0, count, set, type, restTime))
            }

            mAdapter = ListView_Adapter(this, items)
            listView.setAdapter(mAdapter)
        }.addOnFailureListener {
            Log.d(TAG, "[OnFailureListener] Error getting data $it")
        }
    }

    fun asyncTodayWeightInfo() {
        database.child("weightInfo").child("bob").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun getCurrentDateString(): String {
        return SimpleDateFormat("YYYY-MM-d").format(Date()).toString()
    }
}

class ListView_Adapter(context: Context, items: List<ListView_Item>?) :
    BaseAdapter() {
    var items: List<ListView_Item>? = null
    var context: Context

    init {
        this.items = items
        this.context = context
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

        return view
    }
}

class ListView_Item(
    val index: Number,
    val count: Number,
    val set: Number,
    val type: String,
    val restTime: Number?
) {
}