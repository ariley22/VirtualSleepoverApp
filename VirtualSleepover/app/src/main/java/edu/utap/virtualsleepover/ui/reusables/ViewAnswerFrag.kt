package edu.utap.virtualsleepover.ui.reusables

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.utap.virtualsleepover.MainViewModel
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import edu.utap.virtualsleepover.R
import edu.utap.virtualsleepover.databinding.FragmentViewAnswerBinding

class ViewAnswerFrag : Fragment(R.layout.fragment_view_answer) {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentViewAnswerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewAnswerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(javaClass.simpleName, "onViewCreated start")
        viewModel.fetchGameInProgress()
        viewModel.setUserInfo()
        viewModel.response1Listener()
        viewModel.response2Listener()

        binding.partnerAnswer.movementMethod = ScrollingMovementMethod()
        binding.userAnswer.movementMethod = ScrollingMovementMethod()
        binding.nextButton.visibility = View.GONE
        binding.currentRoundTV.text = viewModel.getCurrentRound().toString()

        viewModel.observeCurrentQuestion().observe(viewLifecycleOwner){
            binding.questionTV.text = it
        }

        viewModel.observePartnerName().observe(viewLifecycleOwner){
            binding.partnerNameText.text = it
        }

        viewModel.response1Snapshot.observe(viewLifecycleOwner){
            if(viewModel.playerNumber == 1) binding.userAnswer.text = it
            else if (it == "") binding.partnerAnswer.text = "Waiting for response..."
            else{
                binding.partnerAnswer.text = it
                binding.nextButton.visibility = View.VISIBLE
            }
            viewModel.fetchGameInProgress()
        }

        viewModel.response2Snapshot.observe(viewLifecycleOwner){
            if(viewModel.playerNumber == 2) binding.userAnswer.text = it
            else if (it == "") binding.partnerAnswer.text = "Waiting for response..."
            else{
                binding.partnerAnswer.text = it
                binding.nextButton.visibility = View.VISIBLE
            }
            viewModel.fetchGameInProgress()
        }

        binding.addButtonP.setOnClickListener{
            var playerNumber = 0
            if (viewModel.playerNumber == 1) playerNumber = 2
            else playerNumber = 1
            Log.d(javaClass.simpleName, "saved partner's answer, passing in playerNumber = $playerNumber")
            viewModel.addToScrapbook(playerNumber){
                addedSnackbar(it)
            }
        }
        binding.addButtonU.setOnClickListener{
            Log.d(javaClass.simpleName, "saved own answer, passing in playerNumber = ${viewModel.playerNumber}")
            viewModel.addToScrapbook(viewModel.playerNumber){
                addedSnackbar(it)
            }
        }

        binding.nextButton.setOnClickListener {
            viewModel.inBetweenRounds = true
            viewModel.readyForNextRound()
            findNavController().navigate(ViewAnswerFragDirections.actionViewAnswerFragmentToWaiting())
        }

        if(viewModel.currentRoundStatic == 2) binding.nextButton.text = "Finish game"

        Log.d(javaClass.simpleName, "onViewCreated end")
    }

    fun addedSnackbar(view: View){
        val snackbar = Snackbar.make(view, "Response added to scrapbook",
            Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopResponse2Listener()
        viewModel.stopResponse1Listener()
    }
}