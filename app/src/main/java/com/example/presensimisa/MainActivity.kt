package com.example.presensimisa

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var idMisa:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(intent.extras != null)
        {
            getDataMisa(intent.getStringExtra("kode_misa").toString())
        }

        btnScan.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Hasil tidak ditemukan", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    // converting the data json
//                    val umat = JSONObject(result.contents)
                    //GET UMAT BY QR CODE

                    val manager = this.supportFragmentManager
                    val dialog = InputKursi()
                    dialog.show(manager, "dialog")

                    val args = Bundle()
//                    args.putString("id_umat", umat.getString("id"))
//                    args.putString("id_misa", umat.getString("id"))
                    args.putString("qr_umat", result.contents)
                    args.putString("id_misa", idMisa)
                    dialog.arguments = args
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // jika format encoded tidak sesuai maka hasil
                    // ditampilkan ke toast
                    Toast.makeText(this, result.contents, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onClick(v: View?) {
        // inisialisasi IntentIntegrator(scanQR)
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setOrientationLocked(false)
        intentIntegrator.initiateScan()
    }

    fun getDataMisa(kode_misa:String){
        val queue = Volley.newRequestQueue(this)
        val progressDialog: ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("loading....")
        progressDialog.setTitle("Menambahkan data presensi")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.show()

        val url = "http://192.168.43.250:8000/api/misabykode/$kode_misa"
        println(url)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressDialog.dismiss()
                try {
                    val misaJSON: JSONObject = response.getJSONObject("data")

                    idMisa = misaJSON.optString("id")
                    tvMisa.text = misaJSON.optString("misa")
                    tvTempat.text = misaJSON.optString("tempat")
                    tvWaktu.text = misaJSON.optString("waktu")
                    tvKode.text = misaJSON.optString("kode_misa")

                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_LONG)
                        .show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, { error ->
                progressDialog.dismiss()
                error.printStackTrace()
                println("masuk error")
                this.finish()
            })
        request.retryPolicy = DefaultRetryPolicy(
            0,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }
}