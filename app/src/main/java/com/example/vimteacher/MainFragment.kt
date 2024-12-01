package com.example.vimteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vimteacher.adapter.QuestionAdapter
import com.example.vimteacher.databinding.FragmentMainBinding
import com.example.vimteacher.viewmodel.QuestionsViewModel

class MainFragment : Fragment(){
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val adapter = QuestionAdapter { question ->
        // Navigate to question fragment
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToQuestionFragment(id = question.questionId))
    }
    private val viewModel: QuestionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        setStatusBarColor()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        binding.buttonCheatSheet.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToCheatSheetFragment())
        }
    }


    private fun setupRecyclerView(){
        binding.recyclerView.adapter = adapter

    }


    private fun observeViewModel(){
        viewModel.getAllQuestions().observe(viewLifecycleOwner) { questions ->
            adapter.submitList(questions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setStatusBarColor() {
        // Set the status bar color to match the toolbar color
        val colorPrimary = resources.getColor(R.color.primary_color, null)

        // Set the status bar color
        activity?.window?.statusBarColor = colorPrimary

        // Optional: Make the status bar icons light (if needed for dark backgrounds)
        WindowInsetsControllerCompat(activity?.window!!, activity?.window?.decorView!!).isAppearanceLightStatusBars = false
    }
}