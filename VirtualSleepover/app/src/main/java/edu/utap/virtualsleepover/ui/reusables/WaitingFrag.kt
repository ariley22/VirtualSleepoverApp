package edu.utap.virtualsleepover.ui.reusables

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import edu.utap.virtualsleepover.MainViewModel
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import edu.utap.virtualsleepover.R
import edu.utap.virtualsleepover.databinding.FragmentWaitingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class WaitingFrag : Fragment(R.layout.fragment_waiting) {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentWaitingBinding? = null
    private var player1Ready = false
    private var player2Ready = false
    private var nextRoundStarted = false
    private var oldQuestion = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentWaitingBinding.bind(view)
        Log.d(javaClass.simpleName, "Waiting frag, onViewCreated start")

        viewModel.observeCurrentQuestion().observe(viewLifecycleOwner){
            if(it != oldQuestion && it != "") oldQuestion = it
            Log.d(javaClass.simpleName, "Old question set to $oldQuestion")
        }

        viewModel.fetchGameInProgress()
        viewModel.setUserInfo()

        viewModel.questionListener()
        viewModel.response1Listener()
        viewModel.response2Listener()
        viewModel.player1ReadyListener()
        viewModel.player2ReadyListener()

        binding.partnerNameTV.text = viewModel.getOtherPlayerName()

        if (viewModel.inBetweenRounds) {
            if(viewModel.currentRoundStatic == 2) binding.writeOrRespondTV.text = "finish game..."
            else binding.writeOrRespondTV.text = "finish round..."

            viewModel.player1ReadySnapshot.observe(viewLifecycleOwner) {
                if (it) {
                    player1Ready = true
                    if (player2Ready){
                        lifecycleScope.launch(Dispatchers.Main) {
                            // Inside a coroutine, you can call suspend functions directly
                            nextRound(view)
                        }
                    }
                }
            }
            viewModel.player2ReadySnapshot.observe(viewLifecycleOwner) {
                if (it) {
                    player2Ready = true
                    if (player1Ready){
                        lifecycleScope.launch(Dispatchers.Main) {
                            // Inside a coroutine, you can call suspend functions directly
                            nextRound(view)
                        }
                    }
                }
            }

        } else {
            if (viewModel.isUserWriting) {
                binding.writeOrRespondTV.text = "respond to the prompt..."
                val playerNumber = viewModel.playerNumber

                if (playerNumber == 1) {
                    viewModel.response2Snapshot.observe(viewLifecycleOwner) {
                        if (it != "") enterGame()
                    }
                } else {
                    viewModel.response1Snapshot.observe(viewLifecycleOwner) {
                        if (it != "") enterGame()
                    }
                }
            } else {
                waitForPrompt(view)
            }
        }

        Log.d(javaClass.simpleName, "Waiting frag, onViewCreated end")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(javaClass.simpleName, "Waiting frag destroyed, listeners removed")
        _binding = null
        viewModel.stopQuestionListener()
        viewModel.stopResponse1Listener()
        viewModel.stopResponse2Listener()
        viewModel.stopPlayer2ReadyListener()
        viewModel.stopPlayer1ReadyListener()
    }

    fun nextRound(view: View) {
        if (!nextRoundStarted) {
                nextRoundStarted = true
                viewModel.isUserWriting = !viewModel.isUserWriting
                viewModel.setDbQuestion("")
                viewModel.currentQuestion = ""
                viewModel.inBetweenRounds = false
                viewModel.currentRoundStatic++

                viewModel.nextRound{
                    findNavController().navigate(WaitingFragDirections.actionWaitFragmentToHome())
                    viewModel.endDeleteGame()
                }

            if(viewModel.currentRoundStatic <= 2) {
                if (viewModel.isUserWriting) {
                    Log.d(javaClass.simpleName, "User is writing. Entering game")
                    enterGame()
                } else {
                    Log.d(
                        javaClass.simpleName, "User is not writing. Waiting for question" +
                                "Current question: ${viewModel.currentQuestion}"
                    )
                    waitForPrompt(view)
                }
            }
        }
    }

    fun waitForPrompt(view: View){
        val binding = FragmentWaitingBinding.bind(view)

        Log.d(javaClass.simpleName, "Rebinding screen. Current question ${viewModel.currentQuestion}" +
                " Old question: $oldQuestion")

        binding.writeOrRespondTV.text = "create a prompt..."

        viewModel.questionSnapshot.observe(viewLifecycleOwner) {
            viewModel.fetchGameInProgress()
            Log.d(javaClass.simpleName, "Snapshot triggered. Current question $it" +
                    " Old question: $oldQuestion")
            if (it != "" && it != oldQuestion) {
                viewModel.currentQuestion = it
                Log.d(javaClass.simpleName, "Current question: $it Navigating back...")
                enterGame()
            }
        }
    }

    fun enterGame() {
        findNavController().navigate(WaitingFragDirections.actionWaitFragmentToQuestionRW())
    }
}