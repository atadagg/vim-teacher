package com.example.vimteacher

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.vimteacher.databinding.FragmentQuestionBinding
import com.example.vimteacher.model.QuestionModel
import com.example.vimteacher.viewmodel.QuestionsViewModel

class QuestionFragment : Fragment() {
    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!

    private val args: QuestionFragmentArgs by navArgs()
    private val viewModel: QuestionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setQuestionById(args.id)

        observeViewModel()

        binding.answerButton.setOnClickListener {
            if (viewModel.isAnswered.value == true) {
                handleNext()
            } else {
                handleAnswer()
            }
        }

        binding.skipButton.setOnClickListener {
            handleNext()
        }
    }

    private fun observeViewModel() {
        viewModel.currentQuestionLiveData.observe(viewLifecycleOwner) { question ->
            if (question != null) {
                binding.questionId.text = "Question ${question.questionId}"
                binding.questionBody.text = question.questionBody
                setupOptions(question)
            }
        }

        viewModel.optionStatuses.observe(viewLifecycleOwner) { statuses ->
            statuses?.let { updateOptionStatuses(it) }
        }

        viewModel.explanations.observe(viewLifecycleOwner) { explanations ->
            if (explanations != null) showExplanations(explanations)
        }

        viewModel.isAnswered.observe(viewLifecycleOwner) { answered ->
            if (answered) {
                binding.skipButton.visibility = View.GONE
                binding.answerButton.text = "Next"
            } else {
                binding.skipButton.visibility = View.VISIBLE
                binding.answerButton.text = "Answer"
            }
        }
    }

    private fun setupOptions(question: QuestionModel) {
        binding.radioGroup.removeAllViews()
        question.options.forEach { option ->
            val radioButton = RadioButton(requireContext()).apply {
                id = option.optionId
                text = option.optionBody
                setTextColor(ResourcesCompat.getColorStateList(resources, R.color.text_color, null))
                isEnabled = true
            }
            binding.radioGroup.addView(radioButton)
        }
    }

    private fun updateOptionStatuses(statuses: Map<Int, String>) {
        for (i in 0 until binding.radioGroup.childCount) {
            val radioButton = binding.radioGroup.getChildAt(i) as RadioButton
            val status = statuses[radioButton.id]
            if (status != null) {
                radioButton.text = "${radioButton.text}, $status"
                radioButton.setTextColor(
                    if (status == "Correct")
                        ResourcesCompat.getColor(resources, R.color.correct_green, null)
                    else
                        ResourcesCompat.getColor(resources, R.color.incorrect_red, null)
                )
                radioButton.isEnabled = false
            }
        }
    }

    private fun handleAnswer() {
        val selectedOptionId = binding.radioGroup.checkedRadioButtonId
        if (selectedOptionId == -1) {
            // No option selected
            return
        }
        viewModel.checkAnswer(selectedOptionId)
    }

    private fun handleNext() {
        if (viewModel.hasNextQuestion()) {
            viewModel.nextQuestion()
        } else {
            showNoMoreQuestionsDialog()
        }
    }
    private fun showExplanations(explanations: List<String>) {
        binding.explanationsCard.visibility = View.VISIBLE
        val container = binding.explanationsContainer
        container.removeAllViews()
        explanations.forEach { explanation ->
            val textView = TextView(requireContext()).apply {
                text = explanation
                textSize = 16f
                setPadding(16, 8, 16, 8)
            }
            container.addView(textView)
        }
    }

    private fun showNoMoreQuestionsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("No More Questions")
            .setMessage("There are no any questions after this question")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}