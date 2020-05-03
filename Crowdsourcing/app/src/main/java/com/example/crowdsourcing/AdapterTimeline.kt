/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * Adapter.kt puts the content from "Timeline.kt" to the XML file
 */
package com.example.crowdsourcing

import ItemListener
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_main.view.*


class AdapterTimeline(
    private val exampleList: List<Timeline.ExampleItem2>,
    private val listener: ItemListener
) :
    RecyclerView.Adapter<AdapterTimeline.ViewHolder>() {

    /**
     * @param: parent: ViewGroup, viewType: Int
     * @return: ViewHolder
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.content_main,
            parent, false
        )

        return ViewHolder(itemView) //return as layout
    }

    /**
     * @param: holder: ViewHolder, position: Int
     * get the item from ExampleItem2 from "Timeline.kt"
     * put it to the item from XML file
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = exampleList[position] //current item in list

        holder.donate(listener) //let each xml component hold data
        Picasso.get().load(currentItem.imageResource).into(holder.imageView)    // display image using imported library
        Picasso.get().load(currentItem.imageResource2).into(holder.imageView2)  // display image using imported library
        holder.textView1.text = currentItem.text1
        holder.textView2.text = currentItem.text2
        holder.textView3.text = currentItem.text3
        holder.textView4.text = currentItem.imageResource.toString()
        holder.uId.text = currentItem.uId

    }

    override fun getItemCount() = exampleList.size

    /**
     * @param: itemView: View
     * get RecyclerView item from "activity_timeline.xml"
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: CircularImageView =
            itemView.imageView4 //create variable component according to xml
        val imageView2: ImageView = itemView.imageView3
        val textView1: TextView = itemView.user_display_content
        val textView2: TextView = itemView.date_display_content
        val textView3: TextView = itemView.body_display_content
        val textView4: TextView = itemView.hidden_img
        val uId: TextView = itemView.hidden

        /* if user clicks donate, it will send the info back */
        val donate_btn = itemView.findViewById<TextView>(R.id.donate_btn)
        fun donate(listener: ItemListener) {
            donate_btn.setOnClickListener {
                listener.onClicked(
                    imageView,
                    uId.text.toString(),
                    textView1.text.toString(),
                    textView4.text.toString()
                )
            }
        }

    }
}