package com.example.rocketreserver

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import apolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.example.rocketreserver.databinding.LaunchListFragmentBinding
import kotlinx.coroutines.channels.Channel

class LaunchListFragment : Fragment() {
    private lateinit var binding: LaunchListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val launches = mutableListOf<LaunchListQuery.Launch>()

        lifecycleScope.launchWhenResumed {
            val response = try {
                apolloClient.query(LaunchListQuery()).execute()
            } catch (e: ApolloException) {
                Log.d("LaunchList", "Failure", e)
                null
            }

            val launches = response?.data?.launches?.launches?.filterNotNull()
            if (launches != null && !response.hasErrors()) {
                val adapter = LaunchListAdapter(launches)
                binding.launches.layoutManager = LinearLayoutManager(requireContext())
                binding.launches.adapter = adapter
                val channel = Channel<Unit>(Channel.CONFLATED)
                channel.trySend(Unit)
                adapter.onEndOfListReached = {
                    channel.trySend(Unit)
                }
            }
        }
        binding = LaunchListFragmentBinding.inflate(inflater)
        return binding.root
    }
}
