package com.arthurnagy.miactive.feature

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.arthurnagy.miactive.MainBinding
import com.arthurnagy.miactive.R.layout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<MainBinding>(this, layout.activity_main)?.let { binding = it }
        binding.setLifecycleOwner(this)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding.viewModel = viewModel

        val connectedBand = viewModel.miBand.bluetooth.getConnectedDevices().find { it.name.contains("mi", true) }
        connectedBand?.let {
            viewModel.miBand.connect(this, connectedBand)
        }
    }

}
