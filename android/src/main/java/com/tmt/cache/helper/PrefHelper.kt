package com.tmt.cache.helper

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlin.reflect.KClass

class PrefHelper(context: Context) {

  private val mPref: SharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

  fun putInt(field: String, data: Int) {
    mPref.edit().putInt(field, data).apply()
  }

  fun putString(field: String, data: String) {
    mPref.edit().putString(field, data).apply()
  }

  fun putBoolean(field: String, data: Boolean) {
    mPref.edit().putBoolean(field, data).apply()
  }

  fun getInt(field: String) : Int? {
    return mPref.getInt(field, 0)
  }

  fun getString(field: String) : String? {
    return mPref.getString(field, null)
  }

  fun getFCMString(field: String) : String? {
    return mPref.getString(field, "")
  }

  fun getBoolean(field: String) : Boolean? {
    return mPref.getBoolean(field, false)
  }

  fun clear() {
    mPref.edit().clear().apply()
  }

  fun putObject(key:String,data: Any) {
    val gson = Gson()
    putString(key,gson.toJson(data))
  }



  fun getObject(key:String,model: KClass<*>):Any?{
    val gson = Gson()
    var obj :Any ?=null
    if(!getString(key).isNullOrEmpty()){
      obj = gson.fromJson(getString(key),model.java)
    }
    return obj
  }

  fun checkObject(key:String): Boolean{
    return mPref.contains(key)

  }
}
