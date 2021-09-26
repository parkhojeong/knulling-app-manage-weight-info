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

import android.widget.TextView

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup

import android.widget.BaseAdapter
import android.widget.ListView
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var database: DatabaseReference
    lateinit var mAdapter: ListView_Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Firebase.database.reference
        database.child("message").setValue("hi~")

        val listView: ListView = findViewById(R.id.listview_list)


        database.child("weightInfo").get().addOnSuccessListener {
            Log.d(TAG, "Got value ${it.value}")

            var items = mutableListOf<ListView_Item>()

            val todayData = it.child("bob").child(getCurrentDateString())
            todayData.children.forEach({
                var set: Number = it.child("set").value?.toString()?.toInt() ?: 0
                var count: Number = it.child("count").value?.toString()?.toInt() ?: 0
                var type: String = it.child("type").value?.toString() ?: ""
                var restTime: Number? = it.child("restTime").value?.toString()?.toInt() ?: 0

                Log.d(TAG, "$set, $count, $type $restTime")
                items.add(ListView_Item(0, count, set, type, restTime))
            })

            mAdapter = ListView_Adapter(this, items)
            listView.setAdapter(mAdapter)
        }.addOnFailureListener {
            Log.d(TAG, "Error getting data ${it}")
        }


        database.child("weightInfo").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.d(TAG, "onDataChange")
                dataSnapshot.children.forEach({ date ->
                    Log.d(
                        TAG,
                        "${date.key} ${date.value.toString()} ~~~"
                    )
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun getCurrentDateString(): String {
        return "2021-09-25"
//        return SimpleDateFormat("YYYY-MM-d").format(Date()).toString()
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