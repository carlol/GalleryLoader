# GalleryLoader
A simple bitmap manager to load image from Android device's gallery

```gradle
implementation 'io.carlol.android:galleryloader:1.0.2'
```
- [x] Simple library for simple task
- [x] Fragment and Activity support
- [x] **Fix Samsung image rotation issue** (this is the goal of the library)

## Sample usage

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLoadFromGalleryView.setOnClickListener {
            GalleryLoader.showGalleryChooser(this) // open the chooser intent for gallery
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // extract bitmap
        val bitmap = GalleryLoader.processGalleryChooserResult(this, requestCode, resultCode, data)

        sampleImageView.setImageBitmap(bitmap)
    }

}
```
