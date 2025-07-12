package xyz.lepisma.pile.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun saveRootPath(context: Context, uri: Uri) {
    val sharedPref = context.getSharedPreferences("pile", Context.MODE_PRIVATE)
    with (sharedPref.edit()) {
        putString("root-path", uri.toString())
        apply()
    }

    /* Also take persistent permissions */
    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
}

/* Return the saved root path if any */
fun loadRootPath(context: Context): Uri? {
    val saved = context.getSharedPreferences("pile", Context.MODE_PRIVATE).getString("root-path", null)

    return if (saved != null) {
        val uri = saved.toUri()
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        uri
    } else {
        null
    }
}