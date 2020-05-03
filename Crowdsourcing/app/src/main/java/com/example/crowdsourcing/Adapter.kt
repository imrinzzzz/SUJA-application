/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * Adapter.kt puts the content from "History.kt" to the XML file
 */
package com.example.crowdsourcing

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycler_view_history.view.*

class Adapter(private val exampleList: List<History.ExampleItem>) :
    RecyclerView.Adapter<Adapter.ExampleViewHolder>() {

    /**
     * @param: parent: ViewGroup, viewType: Int
     * @return: ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_history, parent, false)
        //create as one of layout attach to parent layout
        return ExampleViewHolder(itemView) //return after place data to xm;
    }

    /**
     * @param: holder: ExampleViewHolder, position: Int
     * get the item from ExampleItem from "History.kt"
     * put it to the item from XML file
     */
    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val currentItem = exampleList[position] //current item is list number
        Log.d("uri", currentItem.imageResource.toString())
        holder.imageView.setImageURI(currentItem.imageResource) //set holder to hold the current item in the list
        Picasso.get().load(currentItem.imageResource).into(holder.imageView)    // display image using imported library
        holder.textView1.text = currentItem.text1
        holder.textView2.text = currentItem.text2
        holder.textView3.text = currentItem.text3

        if (currentItem.status) {
            holder.textView3.text = "+ " + currentItem.text3
            holder.textView3.setTextColor(Color.parseColor("#17B643"))
        } else {
            holder.textView3.text = "- " + currentItem.text3
            holder.textView3.setTextColor(Color.parseColor("#F40000"))
        }
    }

    override fun getItemCount() = exampleList.size

    /**
     * @param: itemView: View
     * get RecyclerView item from "activity_history.xml"
     */
    class ExampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: CircularImageView = itemView.history_follower //get item according to the xml
        val textView1: TextView = itemView.history_user
        val textView2: TextView = itemView.history_status
        val textView3: TextView = itemView.history_money
    }
}