package com.example.presensimisa

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var listUmat:List<Umat>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // attaching onclickListener
        btnScan.setOnClickListener(this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Hasil tidak ditemukan", Toast.LENGTH_SHORT).show()
            } else {
                // jika qrcode berisi data
                try {
                    // converting the data json
                    val umat = JSONObject(result.contents)

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle(umat.getString("id"))
                    builder.setMessage(umat.getString("nama"))
                    val alert1: AlertDialog = builder.create()
                    alert1.show()
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
//        var intentIntegrator = IntentIntegrator(this)
//        intentIntegrator.setOrientationLocked(false)
//        intentIntegrator.initiateScan()
        tambahBuku()
    }

    fun tambahBuku() {
        val queue = Volley.newRequestQueue(this)
        val progressDialog: ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("loading....")
        progressDialog.setTitle("Menambahkan data presensi")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.show()

        val jsonobj = JSONObject()
        jsonobj.put("id_misa", 1)
        jsonobj.put("id_umat", 1)

        val url = "http://10.0.2.2:8000/api/presensi"
        val request = JsonObjectRequest(Request.Method.POST, url, jsonobj,
            { response ->
                progressDialog.dismiss()
                try {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, { error ->
                progressDialog.dismiss()
                error.printStackTrace()
            })
        request.retryPolicy = DefaultRetryPolicy(
            0,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }
}