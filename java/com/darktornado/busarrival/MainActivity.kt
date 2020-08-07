package com.darktornado.busarrival

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

class MainActivity : Activity() {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, "도움말")
        menu.add(0, 1, 0, "오픈 소스 라이선스")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> showDialog("도움말", " 버스 정류장 이름 또는 정류장 번호를 통해, 해당 버스 정류장에 도착할 예정인 버스 목록을 확인할 수 있는 앱입니다.\n\n 코틀린 연습용으로 만들어본 앱으로, 사로로님의 버스 도착 정보 API를 사용하였습니다.")
            1 -> startActivity(Intent(this, LicenseActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#0288D1")))
        val layout = LinearLayout(this)
        layout.orientation = 1
        val txt1 = TextView(this)
        txt1.text = "버스 정류장 이름 : "
        txt1.textSize = 18f
        txt1.setTextColor(Color.BLACK)
        layout.addView(txt1)
        val txt2 = EditText(this)
        txt2.hint = "정류장 이름 입력..."
        txt2.setTextColor(Color.BLACK)
        txt2.setSingleLine(true)
        layout.addView(txt2)

        val btn = Button(this)
        btn.text = "도착 정보 조회"
        btn.setOnClickListener({
            val input = txt2.text.toString()
            if (input == "") {
                toast("입력된 내용이 없습니다.")
            } else {
                Thread({
                    searchData(input)
                }).start()
                toast("도착 정보를 불러오고 있습니다...");
            }
        })
        layout.addView(btn)

        val maker = TextView(this)
        maker.text = "\n© 2020 Dark Tornado, All rights reserved.\n"
        maker.textSize = 13f
        maker.setTextColor(Color.BLACK)
        maker.gravity = Gravity.CENTER
        layout.addView(maker)

        val pad = dip2px(20)
        layout.setPadding(pad, pad, pad, pad)
        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    fun searchData(input: String) {
        try {
            val data0 = Jsoup.connect("https://saroro.develope.dev/bus/infoByStn/index.php")
                    .data("stn", input).ignoreContentType(true).ignoreHttpErrors(true).get()
            try {
                val data = JSONArray(data0.wholeText())
                val titles = arrayOfNulls<String>(data.length())
                val result = arrayOfNulls<String>(data.length())
                for (n: Int in 0 until data.length()) {
                    val dd = data[n] as JSONObject
                    titles[n] = dd["displayName"] as String + " (" + dd["stopId"] as String + ")\n";
                    titles[n] += dd["address"] as String + "\n";
                    titles[n] += dd["direction"] as String + " 방면";
                    result[n] = ""
                    val list = dd["busInfo"] as JSONArray
                    for (m: Int in 0 until list.length()) {
                        var str = "";
                        val ddd = (list[m] as JSONObject)["busArrive"] as JSONObject
                        if (!(ddd["isRunning"] as Boolean)) continue;
                        if (result[n] != "") str += "\n";
                        str += (list[m] as JSONObject)["name"] as String + "번 버스 : "
                        try {
                            val count = ddd.getInt("prevCount1");
                            str += "$count 정류장 전"
                        } catch (e: Exception) {
                            continue
                        }
                        result[n] += str
                    }
                }
                runOnUiThread { showData(titles, result); }
            } catch (e: JSONException) {
                val data = JSONObject(data0.wholeText())
                toast(data["result"] as String)
            }
        } catch (e: Exception) {
            toast(e.toString())
        }
    }

    fun showData(titles: Array<String?>, result: Array<String?>) {
        try {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("버스 운행정보 조회")
            val list = ListView(this)
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titles)
            list.adapter = adapter
            list.setOnItemClickListener { adapterView, view, pos, id ->
                if (result[pos]!!.trim() == "") toast("도작 예정인 버스가 없습니다");
                else showDialog("버스 도착 정보", result[pos])
            }
            val pad = dip2px(10)
            list.setPadding(pad, pad, pad, pad)
            dialog.setView(list)
            dialog.setNegativeButton("닫기", null)
            dialog.show()
        } catch (e: Exception) {
            toast(e.toString())
        }
    }


    fun showDialog(title: String, msg: CharSequence?) {
        try {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(title)
            dialog.setMessage(msg)
            dialog.setNegativeButton("닫기", null)
            dialog.show()
        } catch (e: Exception) {
            toast(e.toString())
        }
    }

    fun toast(msg: String?) {
        runOnUiThread({
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        })
    }

    fun dip2px(dips: Int): Int {
        return Math.ceil((dips * this.resources.displayMetrics.density).toDouble()).toInt()
    }

}
