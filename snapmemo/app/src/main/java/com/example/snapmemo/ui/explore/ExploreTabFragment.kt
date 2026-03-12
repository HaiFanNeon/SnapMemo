package com.example.snapmemo.ui.explore

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.snapmemo.R

class ExploreTabFragment : Fragment() {

    companion object {
        private const val ARG_POSITION = "position"
        fun newInstance(position: Int) = ExploreTabFragment().apply {
            arguments = Bundle().apply { putInt(ARG_POSITION, position) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val position = arguments?.getInt(ARG_POSITION) ?: 0
        val labels = listOf("公开笔记功能即将上线", "热门标签功能即将上线", "推荐功能即将上线")
        return TextView(requireContext()).apply {
            text = labels.getOrElse(position) { "功能即将上线" }
            gravity = Gravity.CENTER
            setTextColor(resources.getColor(R.color.text_secondary, null))
        }
    }
}
