package edu.utap.virtualsleepover.ui.reusables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.utap.virtualsleepover.MainViewModel
import android.util.Log
import androidx.fragment.app.activityViewModels
import edu.utap.virtualsleepover.R
import edu.utap.virtualsleepover.databinding.FragmentQuestionRwBinding
import androidx.navigation.fragment.findNavController

class QuestionRWFragment : Fragment(R.layout.fragment_question_rw) {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentQuestionRwBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionRwBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(javaClass.simpleName, "RW frag, onViewCreated start")
        Log.d(javaClass.simpleName, "Fragment entered. current question = ${viewModel.currentQuestion}" +
                "isUserWriting = ${viewModel.isUserWriting}")

        viewModel.inBetweenRounds = false

        viewModel.fetchGameInProgress()
        viewModel.setUserInfo()

        Log.d(
            javaClass.simpleName,
            "Check 3. current question = ${viewModel.currentQuestion}" +
                    "isUserWriting = ${viewModel.isUserWriting}"
        )
        binding.questionTV.visibility = View.GONE
        binding.randomButton.visibility = View.GONE

        viewModel.observeIsUserWriting().observe(viewLifecycleOwner) {
            Log.d(javaClass.simpleName, "OBSERVER TRIGGERED. Value = $it")
            viewModel.timeToAnswer = !it
            Log.d(javaClass.simpleName, "Set view by task. timeToAnswer = ${viewModel.timeToAnswer}")
            setViewByTask(viewModel.timeToAnswer)
        }

        binding.submitButton.setOnClickListener {
            //User has just finished writing their question
            Log.d(javaClass.simpleName, "Submit button clicked. timeToAnswer = ${viewModel.timeToAnswer}")

            if (!viewModel.timeToAnswer){
                viewModel.timeToAnswer = true
                Log.d(javaClass.simpleName, "User has submitted a question")
                submitQuestion()
            }

            //User has just finished writing their answer
            else{
                Log.d(javaClass.simpleName, "User has submitted an answer")
                submitAnswer()
            }
        }
        Log.d(javaClass.simpleName, "RW frag, onViewCreated end")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setViewByTask(timeToAnswer: Boolean){
        binding.currentRoundTV.text = viewModel.getCurrentRound().toString()

        if(!timeToAnswer){
            binding.questionTV.visibility = View.GONE
            binding.randomButton.visibility = View.VISIBLE
            binding.writeMeET.hint = "Write your question here"
            binding.randomButton.setOnClickListener{
                viewModel.netTruthQuestion()
                viewModel.observeGenQuestion().observe(viewLifecycleOwner) {
                    binding.writeMeET.setText(it)
                }
            }
        }
        else{
            binding.questionTV.visibility = View.VISIBLE
            viewModel.observeCurrentQuestion().observe(viewLifecycleOwner){
                binding.questionTV.text = it
            }
            binding.writeMeET.hint = "Answer question here"
            binding.writeMeET.setText("")
            binding.randomButton.visibility = View.GONE
        }
    }

    private fun submitQuestion(){
        //create answer writing screen
        viewModel.setDbQuestion(binding.writeMeET.text.toString())
        viewModel.currentQuestion = ""
        setViewByTask(true)
    }

    private fun submitAnswer(){
        val playerNumber = viewModel.playerNumber
        val userAnswer = binding.writeMeET.text.toString()
        if(playerNumber == 1){
            viewModel.setPlayer1Response(userAnswer)
        }
        else{
            viewModel.setPlayer2Response(userAnswer)
        }
        findNavController().navigate(QuestionRWFragmentDirections.actionQuestionRWToViewAnswer())
    }
}