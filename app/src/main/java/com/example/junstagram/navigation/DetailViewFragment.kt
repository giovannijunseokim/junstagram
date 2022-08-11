package com.example.junstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.junstagram.MyApplication
import com.example.junstagram.R
import com.example.junstagram.databinding.FragmentDetailBinding
import com.example.junstagram.databinding.ItemHomefeedBinding
import com.example.junstagram.navigation.model.ContentDataModel
import com.example.junstagram.navigation.recycler.MyAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.storage.FirebaseStorage

class DetailViewFragment : Fragment(){


    override fun onCreateView(inflater: LayoutInflater , container: ViewGroup?, savedInstanceState: Bundle?)
    : View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,
        container,false)

        val binding = FragmentDetailBinding.inflate(layoutInflater)
        binding.detailViewFragmentRecyclerView.adapter = MyAdapter()
        binding.detailViewFragmentRecyclerView.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

}