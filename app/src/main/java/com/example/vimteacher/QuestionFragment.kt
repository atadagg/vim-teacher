package com.example.vimteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.vimteacher.databinding.FragmentQuestionBinding
import com.example.vimteacher.viewmodel.QuestionsViewModel


class QuestionFragment : Fragment() {
    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!

    private val args: QuestionFragmentArgs by navArgs()
    private val viewModel: QuestionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var question = viewModel.getQuestionById(args.id)
        viewModel.setQuestionById(args.id)
        viewModel.currentQuestionLiveData.observe(viewLifecycleOwner){ question ->
            binding.questionId.id = question.questionId
            binding.radioGroup.removeAllViews()
            val container = binding.explanationsCard.findViewById<LinearLayout>(R.id.explanationsContainer)
            container.removeAllViews()
            question.options.forEach{ option ->
                val radioButton = RadioButton(requireContext()).apply {
                    id = option.optionId
                    text = option.optionBody
                    setTextColor(ResourcesCompat.getColorStateList(resources, R.color.radio_button_text_color, null))
                    buttonTintList = ResourcesCompat.getColorStateList(resources, R.color.radio_button_text_color, null)
                }
                binding.radioGroup.addView(radioButton)
                val explanationView = TextView(requireContext()).apply {
                    text = option.optionDescription
                    textSize = 16f
                    setPadding(16, 8, 16, 8)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                container.addView(explanationView)
            }

            binding.questionBody.text = question.questionBody
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            binding.explanationsCard.visibility = View.VISIBLE

            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)

            // Handle all radio buttons
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton

                if (radioButton.id == question?.correctOptionId) {
                    radioButton.isEnabled = true
                    radioButton.isChecked = true  // Show that this was the correct answer
                }
                // If this was selected and it's wrong, make it red
                else if (radioButton == selectedRadioButton) {
                    radioButton.isEnabled = false
                    radioButton.isChecked = true
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}