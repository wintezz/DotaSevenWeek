package com.alexpetrov.dotaheroes.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alexpetrov.dotaheroes.data.HeroModel
import com.alexpetrov.dotaheroes.databinding.ActivityMainBinding
import com.alexpetrov.dotaheroes.ui.interfaces.Listener
import com.alexpetrov.dotaheroes.ui.adapter.HeroAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.*
import java.io.*

class MainActivity : AppCompatActivity(), Listener {

    private lateinit var binding: ActivityMainBinding
    private val urlHeroInfo = "https://api.opendota.com/api/heroStats"
    private val okHttpClient = OkHttpClient()
    private val file: String = "dota"
    private var json: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getHeroModel()
        while (heroInfo.isEmpty()) {
            continue
        }

        readModel()
        initRecycler()
    }

    private fun getHeroModel() {
        val request = Request.Builder()
            .url(urlHeroInfo)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json: String = response.body.string()
                val moshi = Moshi.Builder().build()
                val listType = Types.newParameterizedType(List::class.java, HeroModel::class.java)
                val adapter: JsonAdapter<List<HeroModel>> = moshi.adapter(listType)
                heroInfo = adapter.fromJson(json)!!
            }

            override fun onFailure(call: Call, e: IOException) {
            }
        })
    }

    private fun initRecycler() = with(binding) {
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = HeroAdapter(this@MainActivity, heroInfo)
    }

    override fun onClickItem(heroModel: List<HeroModel>, position: Int) {
        val intentHero = Intent(this, SecondActivity::class.java)
        intentHero.putExtra("id", position)
        startActivity(intentHero)
    }

    private fun readModel() {
        try {
            val br = BufferedReader(
                InputStreamReader(
                    openFileInput(file)
                )
            )
            var str = ""
            str = br.readLine()
            if (str != null) {
                val moshi = Moshi.Builder().build()
                val listType = Types.newParameterizedType(List::class.java, HeroModel::class.java)
                val adapter: JsonAdapter<List<HeroModel>> = moshi.adapter(listType)
                heroInfo = adapter.fromJson(str)!!

            } else {
                getHeroModel()
                while (heroInfo.isEmpty()) {
                    continue
                }
                saveModel()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveModel() {
        try {
            val bw = BufferedWriter(
                OutputStreamWriter(
                    openFileOutput(file, MODE_PRIVATE)
                )
            )
            bw.write(json)
            bw.close()
            Log.d("MY_LOG", "File saved")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        var heroInfo = listOf<HeroModel>()
    }
}