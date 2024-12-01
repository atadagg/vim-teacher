package com.example.vimteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.vimteacher.adapter.QuestionAdapter
import com.example.vimteacher.databinding.FragmentMainBinding
import com.example.vimteacher.viewmodel.QuestionsViewModel

class MainFragment : Fragment(){
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val adapter = QuestionAdapter()
    private val viewModel: QuestionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
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
}