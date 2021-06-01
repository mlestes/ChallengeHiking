package com.coolcats.challengehiking.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.coolcats.challengehiking.databinding.FeedFragmentLayoutBinding
import com.coolcats.challengehiking.db.HikeDB.Companion.getHikeDB
import com.coolcats.challengehiking.db.UserDB
import com.coolcats.challengehiking.mod.Hike
import com.coolcats.challengehiking.util.CHStatus
import com.coolcats.challengehiking.util.Logger.Companion.logE
import com.coolcats.challengehiking.view.adapter.HikeFeedAdapter
import com.coolcats.challengehiking.viewmod.AppViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FeedFragment : Fragment() {

    private val viewModel: AppViewModel by activityViewModels()
    private val database = getHikeDB()
    private lateinit var binding: FeedFragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FeedFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HikeFeedAdapter()
        binding.feedView.adapter = adapter

        val user = UserDB.user

        viewModel.statusData.value = CHStatus.LOADING
        database.child(user.id).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Hike>()
                snapshot.children.forEach { snap ->
                    snap.getValue(Hike::class.java)?.let { hike ->
                        list.add(hike)
                    }
                }
                adapter.hikeList = list
                viewModel.statusData.value = CHStatus.SUCCESS
            }

            override fun onCancelled(error: DatabaseError) {
                viewModel.statusData.value = CHStatus.ERROR
                logE(error.message)
            }

        })
    }

}