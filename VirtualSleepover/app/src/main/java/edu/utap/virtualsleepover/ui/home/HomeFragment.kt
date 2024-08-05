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

        //these functions post any existing user & game data from database to view model
        //posts null/default data to view model if there is no data yet
        viewModel.fetchGameInProgress()
        var partnerName = ""

        viewModel.observeUID().observe(viewLifecycleOwner) {
            viewModel.setUserInfo()
        }

        binding.currentlyConnectedTV.visibility = View.GONE
        binding.partnerNameTV.visibility = View.GONE
        binding.disconnectButton.visibility = View.GONE
        binding.gameInProgressTV.visibility = View.GONE
        binding.continueButton.visibility = View.GONE

        viewModel.observePartnerName().observe(viewLifecycleOwner) {
            partnerName = it
            if (it.isNotEmpty()) {
                binding.currentlyConnectedTV.visibility = View.VISIBLE
                binding.partnerNameTV.visibility = View.VISIBLE
                binding.partnerNameTV.text = it
                binding.disconnectButton.visibility = View.VISIBLE
            }
        }

        viewModel.observeGameID().observe(viewLifecycleOwner) {
            //executes if an actual game in progress is found
            if (it.isNotEmpty()) {
                binding.gameInProgressTV.visibility = View.VISIBLE
                binding.continueButton.visibility = View.VISIBLE
                binding.TenQuestionsButton.setOnClickListener {
                    val snackbar = Snackbar.make(
                        view, "Please finish game in progress first",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.show()
                }
            }
            //executes if variable only contains null game (new game will be created)
            else {
                binding.TenQuestionsButton.setOnClickListener {
                    viewModel.createGame(1)
                    //Check if the user has a partner from previous game
                    //If so, the game will start with this partner
                    //If not, they will be asked to connect to one
                    if (partnerName.isEmpty()) toConnectScreen()
                    else {
                        enterGame()
                    }

                }
            }
        }

        binding.EnterIdButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToJoin())
        }

        binding.continueButton.setOnClickListener {
            if (partnerName == "") toConnectScreen()
            else enterGame()
        }

        binding.disconnectButton.setOnClickListener {
            viewModel.disconnectUser()
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