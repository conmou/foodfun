package edu.foodfun.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.uistate.PartyUIState
import java.text.SimpleDateFormat
import java.util.*

class PartyRecyclerViewAdapter(private val data : MutableList<PartyUIState>, private val clickListener: ItemClickListener)
    : RecyclerView.Adapter<PartyRecyclerViewAdapter.PartyViewHolder>(), Filterable{

    private var dataFiltered = mutableListOf<PartyUIState>()

    class PartyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.labTitle)
        val content: TextView = itemView.findViewById(R.id.labContent)
        val restaurantName: TextView = itemView.findViewById(R.id.labRestaurantName)
        val userCount: TextView = itemView.findViewById(R.id.labMaxMember)
        val time: TextView = itemView.findViewById(R.id.labStartTime)
        val lock: ImageView = itemView.findViewById(R.id.imgLock)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<PartyUIState>()
                if (constraint.isNullOrEmpty())
                    filteredList.addAll(data)
                else
                    data.forEach { if(constraint in it.party!!.title!!) filteredList.add(it) }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                dataFiltered = if(results?.values == null)
                    mutableListOf()
                else
                    results.values as MutableList<PartyUIState>
                notifyDataSetChanged()
            }
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                data.forEach { if(constraint.toString() !in it.party!!.title!!) data.remove(it) }
//                val results = FilterResults()
//                results.values = data
//                return results
//            }
//
//            @SuppressLint("NotifyDataSetChanged")
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                results!!.values as MutableList<PartyUIState>
//                notifyDataSetChanged()
//            }
        }
    }

    interface ItemClickListener {
        fun itemClick(holder: PartyViewHolder, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyViewHolder {
        return PartyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_party, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PartyViewHolder, position: Int) {
        holder.apply {
            if(dataFiltered.size == 0 ) dataFiltered = data
            val party = dataFiltered[position].party!!
            val restaurant = dataFiltered[position].restaurant!!
            val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault(Locale.Category.FORMAT))
            val timeDifference = 28800
            formatter.timeZone = TimeZone.getTimeZone("asia/taipei")

            title.text = party.title
            content.text = party.content
            restaurantName.text = "${restaurant.name}"
            userCount.text = "${party.users.size}/${party.maxMember}"
            time.text = formatter.format(Date((party.reservation!!.seconds + timeDifference) * 1000 )).toString()
            if(party.password?.isNotEmpty() == true) lock.isVisible = true
            itemView.setOnClickListener {
                clickListener.itemClick(holder, position)
            }
        }
    }

    override fun getItemCount(): Int = if(dataFiltered.size == 0) data.size else dataFiltered.size
}