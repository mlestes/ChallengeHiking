package com.coolcats.challengehiking.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.FeedItemLayoutBinding
import com.coolcats.challengehiking.mod.Hike

class HikeFeedAdapter : RecyclerView.Adapter<HikeFeedAdapter.HikeViewHolder>() {

    private lateinit var binding: FeedItemLayoutBinding

    var hikeList: List<Hike> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class HikeViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikeViewHolder {
        binding = FeedItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return HikeViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: HikeViewHolder, position: Int) {
        val hike = hikeList[position]
        binding.locTxt.text = "${hike.startLocation} to ${hike.endLocation}"
        binding.challengeTxt.text = "${hike.challenges} challenges"
        binding.distanceTxt.text = "${hike.distance} miles"
        binding.timeTxt.text = "${hike.time} hours"
        binding.dateTxt.text = hike.date

        }


    override fun getItemCount(): Int = hikeList.size

}