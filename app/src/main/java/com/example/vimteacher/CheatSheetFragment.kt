package com.example.vimteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.vimteacher.R
import com.example.vimteacher.databinding.FragmentCheatSheetBinding

class CheatSheetFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCheatSheetBinding.inflate(inflater, container, false)

        // Set up the Toolbar as ActionBar
        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        // Set the title of ActionBar to be empty or else there are two titles.
        (activity as AppCompatActivity).supportActionBar?.title = ""

        // Set the status bar color to match the toolbar color
        setStatusBarColor()

        return binding.root
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