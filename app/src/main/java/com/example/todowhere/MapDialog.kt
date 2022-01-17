package com.example.todowhere

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.todowhere.databinding.ActivityMapPopUpBinding

class MapDialog : DialogFragment() {

    private lateinit var binding: ActivityMapPopUpBinding

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = ActivityMapPopUpBinding.inflate(inflater,container,false)

        binding.addressTextView.text = "Asdf"

        // 취소 버튼 클릭 시
        binding.backBtn.setOnClickListener {
            dismiss()
        }

        return binding.root

    }


}