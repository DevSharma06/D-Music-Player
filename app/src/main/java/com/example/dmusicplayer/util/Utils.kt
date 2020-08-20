package com.example.dmusicplayer.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat


class Utils {

    companion object {
        public fun checkPermissions(context: Context, permissions: Array<String>) : Boolean {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }

        @Suppress("DEPRECATION")
        public fun isNetworkAvailable(context: Context): Boolean {
            var result = false
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.run {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                        result = when {
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            else -> false
                        }
                    }
                }
            } else {
                connectivityManager.run {
                    connectivityManager.activeNetworkInfo?.run {
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            return true
                        } else if (type == ConnectivityManager.TYPE_MOBILE) {
                            return true
                        }
                    }
                }
            }
            return result
        }
    }
}