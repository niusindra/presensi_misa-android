package com.example.presensimisa

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.input_kursi_fragment.*
import kotlinx.android.synthetic.main.input_kursi_fragment.view.*
import org.json.JSONException
import org.json.JSONObject

class InputKursi : DialogFragment(){
    private var progressDialog: ProgressDialog? = null
    private lateinit var nama:String
    private lateinit var idUmat:String
    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.input_kursi_fragment, container, false)

        val qrUmat = this.arguments!!.getString("qr_umat", "")
        val idMisa = this.arguments!!.getString("id_misa", "")

        this.getDataUmat(qrUmat)

        v.btnTambah.setOnClickListener(View.OnClickListener {
            val queue = Volley.newRequestQueue(context)
            val progressDialog: ProgressDialog = ProgressDialog(context)
            progressDialog.setMessage("loading....")
            progressDialog.setTitle("Menambahkan data presensi")
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.show()

            val jsonobj = JSONObject()
            jsonobj.put("id_misa", idMisa)
            jsonobj.put("id_umat", idUmat)
            jsonobj.put("no_kursi", v.etKursi.text)

            val url = "http://192.168.43.250:8000/api/storepresensi"
            val request = JsonObjectRequest(
                Request.Method.POST, url, jsonobj,
                { response ->
                    progressDialog.dismiss()
                    try {
                        Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG)
                            .show()
                        if(response.getString("message") == "Tambah data berhasil" || response.getString("message") == "Umat sudah hadir")
                            this.dismiss()
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
        })

        return v
    }


    fun getDataUmat(qr:String){
        val queue = Volley.newRequestQueue(context)
        val progressDialog: ProgressDialog = ProgressDialog(context)
        progressDialog.setMessage("loading....")
        progressDialog.setTitle("Menambahkan data presensi")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.show()

        val url = "http://192.168.43.250:8000/api/umatbyqr/$qr"
        println(url)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressDialog.dismiss()
                try {
                    val misaJSON: JSONObject = response.getJSONObject("data")

                    idUmat=misaJSON.optString("id")
                    nama=misaJSON.optString("nama_lengkap")
                    tvDeskripsi.text = misaJSON.optString("nama_lengkap").toString()+" / "+misaJSON.optString("lingkungan").toString()
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