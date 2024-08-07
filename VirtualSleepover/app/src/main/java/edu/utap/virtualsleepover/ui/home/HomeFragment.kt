package edu.utap.virtualsleepover.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import edu.utap.virtualsleepover.databinding.FragmentHomeBinding
import edu.utap.virtualsleepover.MainViewModel
import android.util.Log
import androidx.fragment.app.activityViewModels
import edu.utap.virtualsleepover.R
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // GAME TYPES: 0 = undefined
    // 1 = 10 Questions
    // 2 = Would You Rather
    // 3 = Truth or Dare
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated start")
        binding.loadingTV.visibility = View.VISIBLE
        binding.gameInProgressTV.visibility = View.GONE
        binding.continueButton.visibility = View.GONE
        binding.DeleteButton.visibility = View.GONE
        binding.TenQuestionsButton.visibility = View.GONE
        binding.EnterIdButton.visibility = View.GONE

        viewModel.observeUID().observe(viewLifecycleOwner) {
            viewModel.setUserInfo()
            viewModel.fetchGameInProgress()
        }

        viewModel.observeGameID().observe(viewLifecycleOwner){
            binding.loadingTV.visibility = View.GONE
            if (it.isNullOrEmpty()){
                binding.gameInProgressTV.visibility = View.GONE
                binding.continueButton.visibility = View.GONE
                binding.DeleteButton.visibility = View.GONE
                binding.TenQuestionsButton.visibility = View.VISIBLE
                binding.EnterIdButton.visibility = View.VISIBLE
            }
            else{
                binding.gameInProgressTV.visibility = View.VISIBLE
                binding.continueButton.visibility = View.VISIBLE
                binding.DeleteButton.visibility = View.VISIBLE
                binding.TenQuestionsButton.visibility = View.GONE
                binding.EnterIdButton.visibility = View.GONE
            }
        }

        binding.TenQuestionsButton.setOnClickListener {
            viewModel.createGame(1){
                viewModel.fetchGameInProgress()
                toConnectScreen()
            }
        }

        binding.EnterIdButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToJoin())
        }

        binding.continueButton.setOnClickListener{
            enterGame()
        }

        binding.DeleteButton.setOnClickListener{
            viewModel.endDeleteGame()
        }

        binding.logoutButton.setOnClickListener {
            viewModel.signOut()
        }

        Log.d(javaClass.simpleName, "onViewCreated end")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun toConnectScreen() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToConnect())
    }

    private fun enterGame() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToQuestionRW())
    }
}