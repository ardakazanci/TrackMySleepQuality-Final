/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        // Get a reference to the binding object and inflate the fragment views.
        // XML Tarafına Layout etiketlerini ekleyince otomatik olarak oluşturulur ve bağlanır.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        // Uygulamanın bağlamına bir referans elde ediyoruz.
        val application = requireNotNull(this.activity).application
        // Veritabanı örneğinin DAO ' suna bir referans elde ediyoruz.
        val dataSource = SleepDatabase.getInstance(context = application).sleepDatabaseDao

        // Yukarıda oluşturulan application ve datasource FactoryClass referansı alıyoruz.
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource = dataSource, application = application)

        // FactoryClass aracılığıyla SleepTrackerViewModel örneği alıyoruz.
        val sleepTrackerViewModel =
                ViewModelProviders.of(
                        this, viewModelFactory).get(SleepTrackerViewModel::class.java)
        // Bu şekilde xml tarafında ki variable a kendi oluşturduğumuz ViewModel class örneğimizi birleştirdil.
        // Artık ViewModel üzerinde yapacağımız işlemler XML - UI Tarafına sağlıklı yansıtılacak.
        binding.sleepTrackerViewModel = sleepTrackerViewModel


        // Geçerli etkinlik, dataBinding ' in yaşam döngüsü sahibi olarak ayarladık.
        binding.setLifecycleOwner(this)


        /**
         * Eğer ki navigateToSleepQuality değeri null değilse start stop işlemi bitmiştir.
         * SleepQualityFragment ' a geçiş yapılabilir.
         */
        sleepTrackerViewModel.navigateToSleepQuality.observe(this, Observer {
            // İlgili gece nesnesi üzerinden id ' sini SleepQualityFragment'a gönderiyoruz.
            night ->
            night?.let {
                this.findNavController().navigate(
                        SleepTrackerFragmentDirections
                                .actionSleepTrackerFragmentToSleepQualityFragment(night.nightId))
                sleepTrackerViewModel.doneNavigating()
            }


        })

        sleepTrackerViewModel.showSnackBarEvent.observe(this, Observer {

            if (it == true) { // Observed state is true.
                Snackbar.make(
                        activity!!.findViewById(android.R.id.content),
                        getString(R.string.cleared_message),
                        Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                sleepTrackerViewModel.doneShowingSnackbar()
            }

        })


        return binding.root
    }
}
