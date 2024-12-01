package com.example.vimteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
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
            question.options.forEach{ option ->
                val radioButton = RadioButton(requireContext())
                radioButton.text = option.optionBody
                binding.radioGroup.addView(radioButton)
            }
            binding.questionBody.text = question.questionBody
        }
//        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
//            if (question != null && checkedId == question.questionId) {
//                TODO("TO BE CONTINUED IN PART 2")
//            }
//        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}