package edu.utap.virtualsleepover.ui.scrapbook

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.utap.virtualsleepover.MainViewModel
import edu.utap.virtualsleepover.R
import edu.utap.virtualsleepover.databinding.FragmentScrapbookBinding

//Source: Firenote demo
class ScrapbookFragment : Fragment(R.layout.fragment_scrapbook) {
        private val viewModel: MainViewModel by activityViewModels()

        // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder#getBindingAdapterPosition()
        // Getting the position of the selected item is unfortunately complicated
        // 3/2024, I think this is broken
        private fun getPos(holder: RecyclerView.ViewHolder) : Int {
            val pos = holder.bindingAdapterPosition
            // notifyDataSetChanged was called, so position is not known
            if( pos == RecyclerView.NO_POSITION) {
                return holder.absoluteAdapterPosition
            }
            return pos
        }

        // No need for onCreateView because we passed R.layout to Fragment constructor
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val binding = FragmentScrapbookBinding.bind(view)
            Log.d(javaClass.simpleName, "onViewCreated")

            val adapter = ScrapbookAdapter(viewModel){
                viewModel.removeScrapbook(it)
            }

            val rv = binding.scrapbookRV
            val itemDecor = DividerItemDecoration(rv.context, LinearLayoutManager.VERTICAL)
            binding.scrapbookRV.addItemDecoration(itemDecor)
            binding.scrapbookRV.adapter = adapter

            viewModel.observeScrapbookList().observe(viewLifecycleOwner) {
                Log.d(javaClass.simpleName, "noteList observe len ${it.size}")
                adapter.submitList(it)
            }
            viewModel.observeScrapbookEmpty().observe(viewLifecycleOwner) {
                if(it) {
                    binding.emptyTV.visibility = View.VISIBLE
                } else {
                    binding.emptyTV.visibility = View.GONE
                }
            }
        }
    }