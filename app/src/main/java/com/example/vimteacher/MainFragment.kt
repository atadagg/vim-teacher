package com.example.vimteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.vimteacher.databinding.FragmentMainBinding
import com.example.vimteacher.viewmodel.QuestionsViewModel

class MainFragment : Fragment(){
    private lateinit var binding: FragmentMainBinding
    private val viewModel: QuestionsViewModel by viewModels()
}