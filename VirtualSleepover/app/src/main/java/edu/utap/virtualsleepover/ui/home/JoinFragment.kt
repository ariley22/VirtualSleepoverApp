package edu.utap.virtualsleepover.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import edu.utap.virtualsleepover.databinding.FragmentJoinBinding
import edu.utap.virtualsleepover.MainViewModel
import android.util.Log
import androidx.fragment.app.activityViewModels
import edu.utap.virtualsleepover.R
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

class JoinFragment : Fragment(R.layout.fragment_join) {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentJoinBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentJoinBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated start")

        binding.gameReadyTV.visibility = View.GONE
        binding.continueButton.visibility = View.GONE

        viewModel.observeDisplayName().observe(viewLifecycleOwner){
            binding.displayNameET.setText(it)
        }

        viewModel.observePlayer1ID().observe(viewLifecycleOwner){
            Log.d(javaClass.simpleName, "Join fragment changed, partner UID: $it")

            if(!it.isNullOrEmpty()){
                binding.gameReadyTV.visibility = View.VISIBLE
                binding.continueButton.visibility = View.VISIBLE
            }
        }

        viewModel.fetchGameInProgress()
        viewModel.setUserInfo()

        binding.checkGameButton.setOnClickListener {
            val gameID = binding.gameIdET.text.toString()

            if(binding.displayNameET.text.isEmpty()){
                val snackbar = Snackbar.make(it, "Please enter display name",
                    Snackbar.LENGTH_LONG)
                snackbar.show()
            }
            else {
                val displayName = binding.displayNameET.text.toString()
                viewModel.addAsPlayerTwo(gameID, displayName)
                viewModel.updateDisplayName(gameID, displayName)
            }
        }

        binding.continueButton.setOnClickListener {
            viewModel.isUserWriting = true
            findNavController().navigate(JoinFragmentDirections.actionJoinFragmentToQuestionRW())
        }
        Log.d(javaClass.simpleName, "onViewCreated end")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}