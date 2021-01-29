# ImageColorPickerView
A custom view that allow your user pick color from the image.

![ImageColorPickerViewDemo](https://raw.githubusercontent.com/kyleliao321/image-color-picker-view/main/assets/demo.jpg)

## Usage
- Declare in xml:
```xml
<com.mingwei.imagecolorpickerview.ImageColorPickerView
	xmlns:picker="http://schemas.android.com/apk/res-auto"
	android:id="@+id/color_picker"
	android:layout_width="400dp"
	android:layout_height="400dp"
	android:padding="20dp"
	picker:pickerStrokeWidth="5dp"
	picker:pickerStrokeColor="@color/white"
	picker:pickerRadius="20dp"
	picker:pickerOffsetX="-10dp"
	picker:pickerOffsetY="-10dp"
	picker:enablePicker="true"   />
```
Note: All the picker's custom attributes are optional.

- Set attributes at runtime:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
	super.onCreate(savedInstanceState)
	setContentView(R.layout.activity_main)

	val pickerView = findViewById<ImageColorPickerView>(R.id.color_picker)

	// All the attributes can be updated at runtime.
	// Be cautious that some of the attributes' unit are
	// in density-independent pixels (dp)
	pickerView.pickerRadius = 50
	pickerView.pickerOffsetX = 5
	pickerView.pickerOffsetY = -10
	pickerView.pickerStrokeColor = Color.BLACK
	pickerView.pickerStrokeWidth = 5

	// ImageColorPickerView supports four kinds of image sources
	pickerView.setImage(R.drawable.android_icon) // drawable resource
	pickerView.setImage(drawable) // android drawable object
	pickerView.setImage(uri) // local image file uri
	pickerView.setImage(bitmap) // andorid bitmap object
}
```
Note: detail on how to set image source can be found in [Example](#example) section.

- Catch event:
```kotlin
/**
* ImageColorPickerView provide callback listener that can catch three types of event:
*      1. onPickStarted: called when user touched down the screen, emit with
*         the first color that user picked.
*      2. onColorUpdated: called when user didn't leave the screen and moved
*         to other place, emit with both previous and current picked color.
*      3. onColorPicked: called when user's finger leave the screen, emit with
*         final pixel color that user picked.
*
* Note: Colors are in ARGB_8888 format.
*/
private val pickColorListener = object : ImageColorPickerView.PickColorListener {
	override fun onPickStarted(@ColorInt color: Int) {
	    Log.d(LOG_TAG, "User started to pick color. First color: $color")
	}
	override fun onColorUpdated(@ColorInt oldColor: Int?, @ColorInt newColor: Int) {
	    Log.d(LOG_TAG, "User pick new color $newColor, update from $oldColr")
	}
	override fun onColorPicked(@ColorInt color: Int) {
	    Log.d(LOG_TAG, "Color $color is picked by user")
	}
}

// attach listener
pickerView.setPickColorListener(pickColorListener)
```

## Example
ImageColorPickerView support four types of sources:

- Bitmap:
```kotlin
class MainActivity : AppCompatActivity() {

	...

	private val PHOTO_GALLERY = 0

	override fun onCreate(savedInstanceState: Bundle?) {
	    super.onCreate(savedInstanceState)
	    setContentView(R.layout.activity_main)

        // bind click listener to open photo gallery
	    val button = findViewById<Button>(R.id.select_image_button)
	    button.setOnClickListener {
		  openGallery()
	    }
	}

	// start an intent to open photo gallery
	private fun openGallery() {
	    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
			  startActivityForResult(it, PHOTO_GALLERY )
	    }
	}

	// catch selected photo
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	    super.onActivityResult(requestCode, resultCode, data)
	    when (requestCode) {
	        PHOTO_GALLERY -> {
	            val uriString = data?.dataString
	            val uri = Uri.parse(uriString)

	            // decode bitmap based on different SDK version
	            val bitmap = if (Build.VERSION.SDK_INT < 28) {
	                MediaStore.Images.Media.getBitmap(contentResolver, uri)
	            } else {
	                val src = ImageDecoder.createSource(contentResolver, uri)
	                ImageDecoder.decodeBitmap(src)
	            }

	            // set bitmap as image source
	            findViewById<ImageColorPickerView>(R.id.color_picker)
	                .setImage(bitmap)
	        }
	    }
	}
}
```
- File URI
```kotlin
class MainActivity : AppCompatActivity() {

	...

	private val PHOTO_GALLERY = 0

	override fun onCreate(savedInstanceState: Bundle?) {
	    super.onCreate(savedInstanceState)
	    setContentView(R.layout.activity_main)

	    // bind click listener to open photo gallery
	    val button = findViewById<Button>(R.id.select_image_button)
	    button.setOnClickListener {
		  openGallery()
	    }
	}

	// start an intent to open photo gallery
	private fun openGallery() {
	    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
			  startActivityForResult(it, PHOTO_GALLERY )
	    }
	}

	// catch selected photo
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	    super.onActivityResult(requestCode, resultCode, data)
	    when (requestCode) {
	        PHOTO_GALLERY -> {
	            val uriString = data?.dataString
	            val uri = Uri.parse(uriString)

	            // set uri as image source
	            findViewById<ImageColorPickerView>(R.id.color_picker)
	                .setImage(uri )
	        }
	    }
	}
}
```
- Drawable
```kotlin
class MainActivity : AppCompatActivity() {

	...

	override fun onCreate(savedInstanceState: Bundle?) {
	    super.onCreate(savedInstanceState)
	    setContentView(R.layout.activity_main)

	    // resolve drawable object
	    val drawable = ResourcesCompat.getDrawable(resources, R.drawable.android_icon, null)

	    findViewById<ImageColorPickerView>(R.id.color_picker)
	        .setImage(drawable!!)
	}
}
```
- Drawable Resource Id
```kotlin
class MainActivity : AppCompatActivity() {

	...

	override fun onCreate(savedInstanceState: Bundle?) {
	    super.onCreate(savedInstanceState)
	    setContentView(R.layout.activity_main)

	    findViewById<ImageColorPickerView>(R.id.color_picker)
	        .setImage(R.drawable.android_icon)
	}
}
```

## Custom Attributes
All of custom attributes have setter can be called with view object, allowing update at runtime.
```xml
<resources>
	 <declare-styleable name="ImageColorPickerView">
		 <attr name="pickerRadius" format="dimension" />
		 <attr name="pickerOffsetX" format="dimension" />
		 <attr name="pickerOffsetY" format="dimension" />
		 <attr name="pickerProbeRadius" format="integer" />
		 <attr name="pickerStrokeWidth" format="dimension" />
		 <attr name="pickerStrokeColor" format="color" />
		 <attr name="enablePicker" format="boolean" />
	 </declare-styleable>
</resources>
```

## Probe Radius
On the android device, when user's finger touched on the screen, ImageColorPickerView can catch event from operating system. The event can provide us which coordination did user touched in (x, y) format.

The problem is, this coordination is too ambiguous, it is impossible that user actually **only** touched on that particular pixel. So, ImageColorPickerView provides additional attributes: `pickerProbeRadius`. This allow ImageColorPickerView to use event's coordination as center and `pickerProbeRadius`  as radius to search surronding area . Then, it will use [Pooling Function](#pooling) to select color from this area.

By default, ImageColorPickerView set `pickerProbeRadius` as 0. That means, it only return the color of the pixel that system thinks user touched on. But you can adjust this value based on your need:

- wih xml:
```xml
<com.mingwei.imagecolorpickerview.ImageColorPickerView
	xmlns:picker="http://schemas.android.com/apk/res-auto"
	android:id="@+id/color_picker"
	android:layout_width="400dp"
	android:layout_height="400dp"
	android:padding="20dp"
	picker:pickerProbeRadius="10"   />
```
- at runtime
```kotlin
findViewById<ImageColorPickerView>(R.id.color_picker)
	.pickerProbeRadius = 10
```

## Pooling
In addition to Probe Radius, ImgeColorPickerView also provide pooling function to define how to select from the probe area. It provides following poolings

- AVERAGE_POOLING: Get the average value from each color space. This is the default pooling function.
- BRIGHTEST_POOLING: This use [Relative Luminance](https://en.wikipedia.org/wiki/Relative_luminance) to calculate how bright each pixel is, and select the one that is brightest.
- DARKEST_POOLING: This use [Relative Luminance](https://en.wikipedia.org/wiki/Relative_luminance) to calculate how bright each pixel is, and select the one that is darkest.

You can change the pooling function by calling:
```kotlin
// using brightest pooling
findViewById<ImageColorPickerView>(R.id.color_picker)
	.setPoolingFunc(PoolingFunction.BRIGHTEST_POOLING)

// using darkest pooling
findViewById<ImageColorPickerView>(R.id.color_picker)
	.setPoolingFunc(PoolingFunction.DARKEST_POOLING)
```
Also, you can defined your own pooling function:
```kotlin
// although not realy useful, this custom pooling
// function will always show the black as picked color
private val customPooling = object : IPoolingFunction {
    override fun exec(pixels: IntArray): Int {
        return Color.BLACK
  }
}

findViewById<ImageColorPickerView>(R.id.color_picker)
	.setPoolingFunc(customPooling )
```
