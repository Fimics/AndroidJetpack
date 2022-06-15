package com.mic.me.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mic.databinding.FragmentMeInfoBinding


class InfoFragment : Fragment() {

    private var _binding: FragmentMeInfoBinding? = null
    private val binding get() = _binding!!

    private val tag_info="info"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMeInfoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        val  liveData = userViewModel.getUserData()

        binding.tvName.text=liveData.value?.address


        liveData.observe(viewLifecycleOwner, object :Observer< User> {
            override fun onChanged(user: User?) {
//                user?.let {
//                    Log.d(tag_info, it?.nickName)
//                    binding.tvName.text=liveData.value?.address
//                }

//                with(user){
//                    binding.tvName.text=this?.address
//                }
//                user.apply {
//                    binding.tvName.text=this?.address
//                }

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}