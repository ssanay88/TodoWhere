package com.example.todowhere

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

import com.example.todowhere.databinding.MapPopupBinding

class MapDialog : DialogFragment() {

    private var binding:MapPopupBinding? = null
    private val MapBinding get() = binding!!

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = MapPopupBinding.inflate(inflater,container,false)
        val view = MapBinding.root

        MapBinding.addressTextView.text = "Asdf"

        // 취소 버튼 클릭 시
        MapBinding.backBtn.setOnClickListener {
            dismiss()
        }

        return view

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}