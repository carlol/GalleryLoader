package io.carlol.android.galleryloader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import timber.log.Timber

object GalleryLoader {

    const val REQUEST_CODE_IMAGE_FROM_GALLERY = 9009

    private const val DEFAULT_CHOOSER_TITLE = "Select Picture"


    fun showGalleryChooser(
        activity: Activity,
        title: String = DEFAULT_CHOOSER_TITLE,
        reqCode: Int = REQUEST_CODE_IMAGE_FROM_GALLERY
    ) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(Intent.createChooser(intent, title), reqCode)
    }

    fun showGalleryChooser(
        fragment: Fragment,
        title: String = DEFAULT_CHOOSER_TITLE,
        reqCode: Int = REQUEST_CODE_IMAGE_FROM_GALLERY
    ) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        fragment.startActivityForResult(Intent.createChooser(intent, title), reqCode)
    }

    fun processGalleryChooserResult(
        context: Context
        , returnedRequestCode: Int, resultCode: Int, data: Intent?
        , requiredRequestCode: Int = REQUEST_CODE_IMAGE_FROM_GALLERY
        , reqWidth: Int = Int.MAX_VALUE, reqHeight: Int = Int.MAX_VALUE
    ): Bitmap? {
        if (resultCode == Activity.RESULT_OK) {
            if (returnedRequestCode == requiredRequestCode) {
                val imageUri = data?.data
                if (imageUri == null) {
                    logger("ImageUri not returned by chooser")
                    return null
                }
                return loadImageFromGallery(context, imageUri, reqWidth, reqHeight)
            }
            logger("returnedRequestCode not match with requiredRequestCode")
            return null
        }
        logger("returnedRequestCode not match with requiredRequestCode")
        return null
    }

    fun loadImageFromGallery(context: Context, imageUri: Uri, reqWidth: Int = Int.MAX_VALUE, reqHeight: Int = Int.MAX_VALUE): Bitmap? {
        try {
            var inputStream = context.contentResolver.openInputStream(imageUri)
            var loadedBitmap = BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, this)
                inputStream.close()
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                inJustDecodeBounds = false
                inputStream = context.contentResolver.openInputStream(imageUri)
                BitmapFactory.decodeStream(inputStream, null, this)
            } ?: return null

            val exif: ExifInterface?

            if (Build.VERSION.SDK_INT > 23) {
                exif = ExifInterface(inputStream)
            } else {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = context.contentResolver.query(imageUri, proj, null, null, null)
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()

                exif = ExifInterface(cursor.getString(column_index))
                cursor.close()
            }

            val exifOrientation =
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

            when (exifOrientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> loadedBitmap = rotateBitmap(loadedBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> loadedBitmap = rotateBitmap(loadedBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> loadedBitmap = rotateBitmap(loadedBitmap, 270f)
                ExifInterface.ORIENTATION_UNDEFINED -> {
                    val orientationDegree = getOrientationDegree(context, imageUri).toFloat()
                    if (orientationDegree > 0 && orientationDegree < 360) {
                        loadedBitmap = rotateBitmap(loadedBitmap, orientationDegree)
                    }
                }
                else -> {
                    // nothing to do here
                }
            }
            return loadedBitmap

        } catch (e: Throwable) {
            Timber.w(e)
            return null
        }
    }


    /*
     * Internal methods
     */

    private fun getOrientationDegree(context: Context, photoUri: Uri): Int {
        try {
            val cursor = context.contentResolver.query(
                photoUri,
                arrayOf(MediaStore.Images.ImageColumns.ORIENTATION)
                , null
                , null
                , null
            )

            if (cursor.count != 1) {
                return -1
            }

            cursor.moveToFirst()

            val orientationDegree = cursor.getInt(0)

            cursor.close()

            return orientationDegree
        } catch (e: Throwable) {
            Timber.w(e)
            return 0
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun logger(log: String) {
        Timber.w("[GalleryLoader]: $log")
    }

}