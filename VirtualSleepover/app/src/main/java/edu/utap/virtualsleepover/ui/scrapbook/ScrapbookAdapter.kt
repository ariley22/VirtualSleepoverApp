package edu.utap.virtualsleepover.ui.scrapbook

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.utap.virtualsleepover.MainViewModel
import edu.utap.virtualsleepover.R
import edu.utap.virtualsleepover.databinding.ScrapbookRowBinding
import edu.utap.virtualsleepover.models.Scrapbook
import java.util.*

//Adapted from source: Firenote demo

class ScrapbookAdapter(private val viewModel: MainViewModel,
                       private val removeScrapbook: (Int)->Unit)
    : ListAdapter<Scrapbook, ScrapbookAdapter.VH>(Diff()) {
    // This class allows the adapter to compute what has changed
    class Diff : DiffUtil.ItemCallback<Scrapbook>() {
        override fun areItemsTheSame(oldItem: Scrapbook, newItem: Scrapbook): Boolean {
            return oldItem.firestoreID == newItem.firestoreID
        }

        override fun areContentsTheSame(oldItem: Scrapbook, newItem: Scrapbook): Boolean {
            return oldItem.firestoreID == newItem.firestoreID
                    && oldItem.byUser == newItem.byUser
                    && oldItem.question == newItem.question
                    && oldItem.textResponse == newItem.textResponse
                    && oldItem.timeStamp == newItem.timeStamp
                    && oldItem.photoResponse == newItem.photoResponse
                    && oldItem.savedByUser == newItem.savedByUser
        }
    }

    private val dateFormat: DateFormat =
        SimpleDateFormat("dd-MM-yyyy kk:mm", Locale.US)

    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder#getBindingAdapterPosition()
    // Getting the position of the selected item is unfortunately complicated
    // This always returns a valid index.
    private fun getPos(holder: VH) : Int {
        val pos = holder.bindingAdapterPosition
        // notifyDataSetChanged was called, so position is not known
        if( pos == RecyclerView.NO_POSITION) {
            return holder.absoluteAdapterPosition
        }
        return pos
    }

    inner class VH(private val scrapbookRowBinding: ScrapbookRowBinding) :
        RecyclerView.ViewHolder(scrapbookRowBinding.root) {
        init {
            scrapbookRowBinding.deleteButton.setOnClickListener {
                removeScrapbook(getPos(this))
            }
        }
        fun bind(holder: VH, position: Int) {
            val scrapbook = viewModel.getScrapbookItem(position)
            holder.scrapbookRowBinding.questionTV.text = scrapbook.question
            holder.scrapbookRowBinding.responseTV.text = scrapbook.textResponse
            scrapbook.timeStamp?.let {
                holder.scrapbookRowBinding.timestamp.text = dateFormat.format(it.toDate())
            }
            if(scrapbook.byUser == "self"){
                holder.scrapbookRowBinding.byUserTV.text = "Your response"
            }
            else{
                holder.scrapbookRowBinding.byUserTV.text = scrapbook.byUser + "\'s response"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val scrapbookRowBinding = ScrapbookRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return VH(scrapbookRowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(holder, position)
    }
}