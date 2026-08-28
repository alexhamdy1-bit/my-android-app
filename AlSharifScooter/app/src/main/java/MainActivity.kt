package com.alsharif.scooter

import android.app.*
import android.os.Bundle
import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("data", Context.MODE_PRIVATE) }
    private val products=JSONArray()
    private val sales=JSONArray()
    private val purchases=JSONArray()
    private val expenses=JSONArray()
    private lateinit var root: LinearLayout
    private fun money(v:Double)="%.2f ج.م".format(Locale.US,v)
    private fun today()=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
    private fun save(k:String,a:JSONArray){prefs.edit().putString(k,a.toString()).apply()}
    private fun load(k:String):JSONArray=JSONArray(prefs.getString(k,"[]"))
    override fun onCreate(b:Bundle?){super.onCreate(b); loadAll(); home()}
    private fun loadAll(){for((k,a) in listOf("products" to products,"sales" to sales,"purchases" to purchases,"expenses" to expenses)){val x=load(k);for(i in 0 until x.length())a.put(x.get(i))}}
    private fun base(title:String):LinearLayout{
        root=LinearLayout(this);root.orientation=LinearLayout.VERTICAL;root.setPadding(18,18,18,18);root.setBackgroundColor(Color.rgb(11,15,20))
        val h=TextView(this);h.text=title;h.setTextColor(Color.WHITE);h.textSize=25f;h.setPadding(4,4,4,18);root.addView(h)
        setContentView(root);return root
    }
    private fun btn(t:String,action:()->Unit):Button{val b=Button(this);b.text=t;b.setTextColor(Color.WHITE);b.setBackgroundColor(Color.rgb(45,58,80));b.setOnClickListener{action()};root.addView(b,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,6,0,6)});return b}
    private fun input(h:String):EditText{val e=EditText(this);e.hint=h;e.setHintTextColor(Color.rgb(130,140,155));e.setTextColor(Color.WHITE);root.addView(e,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)});return e}
    private fun home(){
        base("الشريف اسكوتر 🛵")
        val d=TextView(this);d.text="نظام المبيعات والمخزون — أوف لاين";d.setTextColor(Color.LTGRAY);d.textSize=15f;root.addView(d)
        var s=0.0;for(i in 0 until sales.length())if(sales.getJSONObject(i).getString("date")==today())s+=sales.getJSONObject(i).getDouble("total")
        var p=0.0;for(i in 0 until purchases.length())if(purchases.getJSONObject(i).getString("date")==today())p+=purchases.getJSONObject(i).getDouble("amount")
        var e=0.0;for(i in 0 until expenses.length())if(expenses.getJSONObject(i).getString("date")==today())e+=expenses.getJSONObject(i).getDouble("amount")
        val info=TextView(this);info.text="\nمبيعات اليوم: ${money(s)}\nمشتريات: ${money(p)}\nمصروفات: ${money(e)}\nصافي حركة اليوم: ${money(s-p-e)}";info.setTextColor(Color.WHITE);info.textSize=18f;root.addView(info)
        btn("➕ إضافة منتج"){product()}
        btn("💰 تسجيل بيع"){sale()}
        btn("🧾 مشتريات الشغل"){purchase()}
        btn("💸 مصروفات"){expense()}
        btn("📋 سجل العمليات"){logs()}
        btn("🔒 إنهاء اليوم"){endDay()}
    }
    private fun product(){
        base("إضافة منتج");val n=input("اسم المنتج");val qty=input("الكمية");qty.inputType=2
        val buy=input("سعر شراء القطعة");buy.inputType=8194
        val sell=input("سعر بيع القطعة");sell.inputType=8194
        btn("حفظ المنتج"){if(n.text.isBlank())return@btn;products.put(JSONObject().apply{put("name",n.text.toString());put("qty",qty.text.toString().toIntOrNull()?:0);put("buy",buy.text.toString().toDoubleOrNull()?:0.0);put("sell",sell.text.toString().toDoubleOrNull()?:0.0)});save("products",products);home()}
        btn("رجوع"){home()}
    }
    private fun sale(){
        base("تسجيل بيع");if(products.length()==0){TextView(this).apply{text="لا توجد منتجات.";setTextColor(Color.WHITE);root.addView(this)};btn("رجوع"){home()};return}
        val names=ArrayList<String>();for(i in 0 until products.length())names.add(products.getJSONObject(i).getString("name"))
        val sp=Spinner(this);sp.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,names);root.addView(sp)
        val q=input("الكمية");q.inputType=2
        btn("حفظ البيع"){val idx=sp.selectedItemPosition;val o=products.getJSONObject(idx);val x=q.text.toString().toIntOrNull()?:0;if(x<1||x>o.getInt("qty")){Toast.makeText(this,"الكمية غير متاحة",Toast.LENGTH_SHORT).show();return@btn};o.put("qty",o.getInt("qty")-x);sales.put(JSONObject().apply{put("date",today());put("product",o.getString("name"));put("qty",x);put("total",x*o.getDouble("sell"))});save("products",products);save("sales",sales);home()}
        btn("رجوع"){home()}
    }
    private fun purchase(){
        base("مشتريات الشغل");val d=input("البيان");val a=input("المبلغ");a.inputType=8194
        btn("تسجيل المشتريات"){val v=a.text.toString().toDoubleOrNull()?:0.0;if(d.text.isBlank()||v<=0)return@btn;purchases.put(JSONObject().apply{put("date",today());put("desc",d.text.toString());put("amount",v)});save("purchases",purchases);home()}
        btn("رجوع"){home()}
    }
    private fun expense(){
        base("مصروفات الشغل");val d=input("البيان");val a=input("المبلغ");a.inputType=8194
        btn("تسجيل المصروف"){val v=a.text.toString().toDoubleOrNull()?:0.0;if(d.text.isBlank()||v<=0)return@btn;expenses.put(JSONObject().apply{put("date",today());put("desc",d.text.toString());put("amount",v)});save("expenses",expenses);home()}
        btn("رجوع"){home()}
    }
    private fun logs(){
        base("سجل العمليات");val t=TextView(this);t.setTextColor(Color.WHITE);t.textSize=15f;val sb=StringBuilder()
        for(i in sales.length()-1 downTo 0){val o=sales.getJSONObject(i);sb.append("بيع — ${o.getString("product")} × ${o.getInt("qty")} — ${money(o.getDouble("total"))}\n")}
        for(i in purchases.length()-1 downTo 0){val o=purchases.getJSONObject(i);sb.append("شراء — ${o.getString("desc")} — ${money(o.getDouble("amount"))}\n")}
        for(i in expenses.length()-1 downTo 0){val o=expenses.getJSONObject(i);sb.append("مصروف — ${o.getString("desc")} — ${money(o.getDouble("amount"))}\n")}
        t.text=if(sb.isEmpty())"السجل فارغ" else sb.toString();root.addView(ScrollView(this).apply{addView(t)},LinearLayout.LayoutParams(-1,0,1f));btn("رجوع"){home()}
    }
    private fun endDay(){
        AlertDialog.Builder(this).setTitle("إنهاء اليوم").setMessage("سيتم عرض ملخص يوم ${today()}.\nيمكنك الاحتفاظ بالبيانات وسجل العمليات.").setPositiveButton("إنهاء"){_,_->summary()}.setNegativeButton("إلغاء",null).show()
    }
    private fun summary(){
        base("ملخص يوم ${today()}");var s=0.0;purchases.let{};for(i in 0 until sales.length()){val o=sales.getJSONObject(i);if(o.getString("date")==today())s+=o.getDouble("total")}
        val t=TextView(this);t.setTextColor(Color.WHITE);t.textSize=20f;t.text="\nإجمالي المبيعات: ${money(s)}\n\nتم إنهاء يوم المبيعات بنجاح.\nالبيانات محفوظة على الجهاز.";root.addView(t);btn("العودة للرئيسية"){home()}
    }
}