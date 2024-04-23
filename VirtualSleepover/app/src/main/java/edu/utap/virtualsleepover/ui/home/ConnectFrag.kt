package edu.utap.virtualsleepover.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import edu.utap.virtualsleepover.databinding.FragmentConnectBinding
import edu.utap.virtualsleepover.MainViewModel
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import edu.utap.virtualsleepover.R

class ConnectFrag : Fragment(R.layout.fragment_connect) {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentConnectBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentConnectBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated start")
        viewModel.fetchGameInProgress()
        viewModel.setUserInfo()
        viewModel.partnerListener()

        binding.successTV.visibility = View.GONE
        binding.continueButton.visibility = View.GONE
        var gameID = ""

        viewModel.observeGameID().observe(viewLifecycleOwner) {
            binding.gameIdTV.text = it
            gameID = it
        }

        viewModel.observeDisplayName().observe(viewLifecycleOwner){
            binding.displayNameET.setText(it)
        }

        viewModel.partnerSnapshot.observe(viewLifecycleOwner) {
            Log.d(javaClass.simpleName, "Connect fragment changed, partner UID: $it")
            if(it != ""){
                binding.successTV.visibility = View.VISIBLE
                binding.successTV.text = "Success! You are now connected"
                binding.continueButton.visibility = View.VISIBLE
            }
        }

        binding.continueButton.setOnClickListener {
            if(binding.displayNameET.text.isEmpty()){
                val snackbar = Snackbar.make(it, "Display name is required",
                    Snackbar.LENGTH_LONG)
                snackbar.show()
            }
            else{
                viewModel.updateDisplayName(gameID, binding.displayNameET.text.toString())
                findNavController().navigate(ConnectFragDirections.actionConnectFragmentToWaiting())
            }
        }

        Log.d(javaClass.simpleName, "onViewCreated end")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopPartnerListener()
        _binding = null
    }
}