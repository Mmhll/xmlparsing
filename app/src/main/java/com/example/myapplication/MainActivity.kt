package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.databinding.ActivityMainBinding
import okhttp3.ResponseBody
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.simpleframework.xml.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var retrofit = Retrofit.Builder()
            .baseUrl("https://geo.madskill.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        binding.map.setTileSource(
            XYTileSource(
                "madMap",
                0,
                50,
                256,
                ".png" ,
                arrayOf("https://map.madskill.ru/osm/")))
        binding.map.setUseDataConnection(true)
        var mapController = binding.map.controller
        mapController.setZoom(7.00)
        //53.20620624551595, 50.23607098112966
        //53.206725879340524, 50.22831362672528

        retrofit.create(RetrofitApi::class.java).getCity("Samara").enqueue(object : Callback<ArrayList<City>>{
            override fun onResponse(call: Call<ArrayList<City>>, response: Response<ArrayList<City>>) {
                if (response.isSuccessful){
                    response.body()!!.let {
                        var startPoint = GeoPoint(it[0].lat.toDouble(), it[0].lon.toDouble())
                        mapController.setCenter(startPoint)
                        var geopoints = arrayListOf<GeoPoint>(
                            startPoint,
                            GeoPoint(53.206725879340524, 50.22831362672528),
                            GeoPoint(53.20620624551595, 50.23607098112966))
                        var roadManager = OSRMRoadManager(this@MainActivity, "agent")
                        roadManager.setService("route")
                        roadManager.setMean("foot")
                        //roadManager.getRoad(geopoints)

                    }
                }
                else
                {
                    Log.d("Fail", response.message())
                }
            }

            override fun onFailure(call: Call<ArrayList<City>>, t: Throwable) {
                Log.d("Fail2", t.message.toString())
            }

        })
        val retro2 = Retrofit.Builder()
            .baseUrl("https://overpass.madskill.ru/api/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
        retro2.create(RetrofitApi::class.java).getDost("node(2);out body;").enqueue(object : Callback<osm>{
            override fun onResponse(call: Call<osm>, response: Response<osm>) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(response.body().toString())
                    .create()
                    .show()
                Log.d("Response code", response.code().toString())
            }

            override fun onFailure(call: Call<osm>, t: Throwable) {
                Log.d("ERROR", t.message.toString())
            }

        })
    }
}

interface RetrofitApi{
    @GET("search")
    fun getCity(@Query("city") city : String) : Call<ArrayList<City>>

    @GET("interpreter")
    fun getDost(@Query("data") node : String) : Call<osm>
}

data class City(val place_id : Int, val lat : String, val lon : String, val display_name : String)

@Root(name = "osm", strict = false)
data class osm(
    @field:ElementList(name = "node") var node : ArrayList<node>? = null,
    @field:Attribute(name = "id", required = false)
    var id : String = "",
    @field:Attribute(name = "lat", required = false)
    var lat : String = "",
    @field:Attribute(name ="lon", required = false)
    var lon : String = ""
)

data class node(
    @field:ElementList(name = "tag", inline = true, required = false)
    var tag : String = "",
    @field:Attribute(name = "k", required = false)
    var key : String = "",
    @field:Attribute(name = "v", required = false)
    var value : String = ""
)