package ru.sample.duckapp

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.sample.duckapp.domain.Duck
import ru.sample.duckapp.infra.Api
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var nextDuckButton: Button
    private lateinit var httpCodeEditText: EditText
    private lateinit var loadWaiter: TextView

    private lateinit var duckWebView: WebView

    private var imgPlaceholder = "IMAGE_PLACEHOLDER"
    private var html = "<html><body><img style='height: 100%; width: 100%; object-fit: contain' src='$imgPlaceholder' /></body></html>"

    val httpCodes = intArrayOf(
        100, 101, 102, 103,
        200, 201, 202, 203, 204, 205, 206, 207, 208, 226,
        300, 301, 302, 303, 304, 305, 306, 307, 308,
        400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417,
        418, 421, 422, 423, 424, 425, 426, 428, 429, 431, 449, 451, 499,
        500, 501, 502, 503, 504, 505, 506, 507, 508, 510, 511, 520, 521, 522, 523, 524, 525, 526
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nextDuckButton = findViewById(R.id.nextDuckButton)
        httpCodeEditText = findViewById(R.id.httpCodeEditText)
        loadWaiter = findViewById(R.id.loadWaiter)
        loadWaiter.setVisibility(View.GONE);

        duckWebView = findViewById(R.id.duckWebView)

        nextDuckButton.setOnClickListener {
            val httpCode = httpCodeEditText.text.toString()
            if (httpCode.isEmpty()) {
                loadRandomDuck()
            } else {
                var code = httpCode.toInt()
                if (checkCode(code)) {
                    loadDuckByCode(code)
                } else {
                    report("Такого HTTP кода нет!")
                }
            }
        }

        loadRandomDuck()
    }

    private fun checkCode(code: Int): Boolean{
        return httpCodes.contains(code);
    }

    private fun setImg(imgStream: InputStream){
        val imgageBase64: String = Base64.encodeToString(
            IOUtils.toByteArray(
                imgStream
            ), Base64.DEFAULT)
        val image = "data:image/png;base64,$imgageBase64"
        duckWebView.loadDataWithBaseURL(
            "file:///android_asset/",
            html.replace(imgPlaceholder, image),
            "text/html",
            "utf-8",
            ""
        )
    }

    private fun loadRandomDuck() {
        loadWaiter.setVisibility(View.VISIBLE);
        Api.ducksApi.getRandomDuck().enqueue(object : Callback<Duck> {
            override fun onResponse(call: Call<Duck>, response: Response<Duck>) {
                if (response.isSuccessful) {
                    val duckData = response.body()
                    duckData?.let {
                        var imgName = it.url.split("/").last()
                        Api.ducksApi.getDuckImage(imgName).enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.isSuccessful) {
                                    val imageStream = response.body()?.byteStream()
                                    if (imageStream != null) {
                                        setImg(imageStream)
                                    }
                                } else {
                                    report("Что-то пошло не так.")
                                }
                                loadWaiter.setVisibility(View.GONE);
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                report("Не удалось подключиться к сайту.")
                                loadWaiter.setVisibility(View.GONE);
                            }
                        })
                    }
                }
                else {
                    report("Что-то пошло не так.")
                }
            }

            override fun onFailure(call: Call<Duck>, t: Throwable) {
                report("Не удалось подключиться к сайту.")
                loadWaiter.setVisibility(View.GONE);
            }
        })
    }

    private fun loadDuckByCode(httpCode: Int) {
        loadWaiter.setVisibility(View.VISIBLE);
        Api.ducksApi.getDuckImageByCode(httpCode).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val imageStream = response.body()?.byteStream()
                    if (imageStream != null) {
                        setImg(imageStream)
                    }
                } else {
                    report("Такой уточки нет(")
                }
                loadWaiter.setVisibility(View.GONE);
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                report("Не удалось подключиться к сайту.")
                loadWaiter.setVisibility(View.GONE);
            }
        })
    }

    private fun report(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}