package bzh.zelyon.music.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

fun Context.dpToPx(int: Int) = int * resources.displayMetrics.density

fun Context.pxToDp(int: Int) = int / resources.displayMetrics.density

fun View.closeKeyboard() {
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
}

fun View.openKeyboard() {
    requestFocus()
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, 0)
}

fun Context.getResIdFromAndroidAttr(androidId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(androidId, typedValue, true)
    return typedValue.resourceId
}

fun Int.millisecondstoDuration(): String = SimpleDateFormat(if (this < 60*60*1000) "mm:ss" else "hh:mm:ss").format(Date(toLong()))

fun ImageView.setImage(model: Any, placeholder: Drawable? = null) {
    Glide.with(this).load(model).apply {
        placeholder?.let { placeholder ->
                placeholder(placeholder)
                error(placeholder)
        }
    }.into(this)
}

fun Context.getLocalFileFromGalleryUri(uri: Uri, optionalName: String? = null): File? {
    val isFile = uri.scheme == ContentResolver.SCHEME_FILE
    val isDocumentUri = DocumentsContract.isDocumentUri(this, uri)
    val isExternalStorage = isDocumentUri && uri.authority == "com.android.externalstorage.documents"
    val isDownloadStorage = isDocumentUri && uri.authority == "com.android.providers.downloads.documents"
    val isImageStorage = isDocumentUri && uri.authority == "com.android.providers.media.documents"
    val isContent = uri.scheme == ContentResolver.SCHEME_CONTENT
    val isGooglePhotosNew = isContent && uri.authority == "com.google.android.apps.photos.contentprovider"
    val isGooglePhotos = isContent && uri.authority == "com.google.android.apps.photos.content"
    val isDrive = isContent && uri.authority == "com.google.android.apps.docs.storage"
    val isContentStorage = isContent && uri.path?.contains("/storage") ?: false

    val path = when {
        isFile -> uri.path
        isExternalStorage -> Environment.getExternalStorageDirectory().toString() + "/" + DocumentsContract.getDocumentId(uri).split(":")[1]
        isDownloadStorage -> getPathFromCursor(ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), DocumentsContract.getDocumentId(uri).toLong()), arrayOf(MediaStore.Images.Media.DATA))
        isImageStorage -> getPathFromCursor(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media.DATA), MediaStore.Images.Media._ID + "=?", DocumentsContract.getDocumentId(uri).split(":")[1])
        isGooglePhotos -> uri.lastPathSegment
        isContentStorage -> uri.path?.substring(uri.path?.indexOf("/storage") ?: 0)
        isDrive || isGooglePhotosNew || isContent -> getPathFromCursor(uri, arrayOf(MediaStore.Images.Media.DATA))
        else -> null
    }

    var name = System.currentTimeMillis().toString().plus(".").plus(getExtension(if (path != null) Uri.parse(path) else uri))
    contentResolver.query(uri, null, null, null, null)?.use {
        if (it.moveToFirst()) {
            it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))?.let { displayName ->
                name = displayName
            }
        }
        it.close()
    }

    val outputFile = createNewFile(optionalName ?: name)
    when {
        path != null -> FileInputStream(File(path))
        Patterns.WEB_URL.matcher(uri.toString()).matches() && URLUtil.isValidUrl(uri.toString()) -> URL(uri.toString()).openStream()
        else -> contentResolver.openInputStream(uri)
    }?.use { inputStream ->
        FileOutputStream(outputFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return if (outputFile.exists()) outputFile else null
}

fun Context.getPathFromCursor(uri: Uri, projection: Array<String>? = null, selection: String? = null, vararg selectionArgs: String): String? {
    try {
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            it.close()
        }
    } catch (e: Exception) {}
    return null
}

fun Context.createNewFile(name: String): File {
    val file = File(externalCacheDir, name)
    return if (file.exists()) createNewFile("_$name") else file
}

fun Context.getExtension(uri: Uri) = if (uri.scheme == ContentResolver.SCHEME_CONTENT) MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri)) else MimeTypeMap.getFileExtensionFromUrl(uri.path)

fun View.vibrate(step: Int = 0) {
    val translations = arrayOf(0F, 25F, -25F, 25F, -25F, 15F, -15F, 5F, -5F, 0F)
    animate().apply {
        duration = 100L
        translationX(translations[step])
        withEndAction { if (step < translations.size-1) vibrate(step + 1) }
    }.start()
}
