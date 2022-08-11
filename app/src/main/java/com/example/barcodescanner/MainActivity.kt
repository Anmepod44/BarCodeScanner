package com.example.barcodescanner

import android.Manifest
import android.Manifest.permission.INTERNET
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.content.Intent.createChooser
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.integration.android.IntentIntegrator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.File
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity() {

//  The scan button and the text views.
    private lateinit var scanButton:Button
    private lateinit var textView:TextView
    private lateinit var imageView:ImageView
    private lateinit var barcodeScanner:BarcodeScanner


    private lateinit var CameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var GalleryLauncher:ActivityResultLauncher<Intent>
    private lateinit var FileLauncher:ActivityResultLauncher<Intent>
    private lateinit var inputImage: InputImage
    private var requestedCode=1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initializing components of the application.
        setContentView(R.layout.activity_main)
        scanButton=findViewById(R.id.click_btn)
        textView=findViewById(R.id.home_txt_view)
        imageView=findViewById(R.id.qr_view)
        barcodeScanner= BarcodeScanning.getClient()

        //setting the title of the barcode scanner application.
        title="Scan BarCode."

        //Check if the application has all the permissions, if not request for the permissions.
        this.checkPermission()

        //This is the camera activity launcher object.
        FileLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult(),object:ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult?) {

                var data = result?.data?.extras?.get("data") as? Bitmap
                println(data.toString())

                //create an instance of the model
                var processor = BarcodeScanning.getClient()

                processQr();
            }
        }
        );


        //Camera Launcher.
        CameraLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult>{
                override fun onActivityResult(result: ActivityResult?) {
                   val data=result?.data

                    try{
                        val photo=data?.extras?.get("data") as? Bitmap
                        inputImage=InputImage.fromBitmap(photo!!,0)
                        processQr()

                    }
                    catch(e:Exception){//
                        e.let {
                            Toast.makeText(this@MainActivity,it?.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
                        }

                    } } })


        //Gallery Launcher.
        GalleryLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult>{
                override fun onActivityResult(result: ActivityResult?) {
                    val data=result?.data
                    //making a toast for the data.

                    try{
                        val photo=data?.extras?.get("data") as? Bitmap
                        inputImage=InputImage.fromFilePath(this@MainActivity,data?.data!!)
                        processQr()

                    }
                    catch(e:Exception){
                        e.let{
                            println("Some thing went wrong ${it?.localizedMessage?.toString()}")

                        }
                    }
                }
            }
        )

        //setting the onclck listener to the button.
      scanButton.setOnClickListener {
          val options= arrayOf("camera","gallery")

             //The alert dialog builder.
            var builder=AlertDialog.Builder(this)
            builder.setTitle("Pick an Option")
             builder.setItems(options, DialogInterface.OnClickListener{
                 dialogInterface, which ->

                if(which==0){
                    val camerIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    CameraLauncher.launch(camerIntent);
               }
                else{
                     val galleryIntent=Intent()
                     galleryIntent.setType("image/*")
                     galleryIntent.setAction(Intent.ACTION_GET_CONTENT)
                    GalleryLauncher.launch(galleryIntent)

                 }
             })
          builder.show()
        }

      }

    //what to do upon acceptance of the permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: kotlin.Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==requestedCode && grantResults.isNotEmpty()){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()

            }
            else{
                 this.checkPermission();
            }
        }

    }
 //This is a function to process the QR code.
private fun processQr(){
    this.imageView.visibility= View.GONE
    this.textView.visibility=View.VISIBLE
    barcodeScanner.process(inputImage!!).addOnSuccessListener {
        //handle the barcode list
        for(barcode: Barcode in it){
            val valueType=barcode.valueType
            when(valueType){
                Barcode.TYPE_WIFI ->{
                    val ssid=barcode.wifi!!.password
                    val password=barcode.wifi!!.password
                    val type=barcode.wifi!!.encryptionType
                    this.textView.text="$ssid, $password, $type"

                }

                Barcode.TYPE_URL ->{
                    val title=barcode.url!!.title
                    val url=barcode.url!!.url

                    //start an activity to open the barcode of type url.
                    Uri.parse(url).let{
                        url->
                        var intent=createChooser(Intent(Intent.ACTION_VIEW,url),"Perform Action Using")
                        startActivity(intent)
                    }
                }
                Barcode.TYPE_PHONE->{
                    var phoneNo=Uri.parse(barcode.phone.toString()).let{
                        uri->
                        startActivity(Intent(ACTION_DIAL,uri))
                    }

                }

                Barcode.TYPE_TEXT ->{
                    val data=barcode.displayValue
                    this.textView.text="data: $data"

                }

                Barcode.FORMAT_QR_CODE->{
                    val data=barcode.valueType
                    this.textView.text="Hello"

                }

                Barcode.TYPE_PRODUCT->{
                    var desc=barcode.displayValue
                    textView.text=desc

                }

            }
        }
    }.addOnFailureListener{
            err->
        Toast.makeText(this@MainActivity,err.localizedMessage.toString(),Toast.LENGTH_SHORT).show()
    }
}

////function to check access permissions of the camera and external storage.
private fun checkPermission(){

    if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
        //request the permission on behalf of the user.
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.CAMERA),requestedCode)

    }

    else if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
        //request the permission on behalf of the user.
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),113)
    }


}

private fun processQRcodes(){

    //setting up the qr code reader.
    val options=BarcodeScannerOptions.Builder().setBarcodeFormats(
        Barcode.FORMAT_QR_CODE,
        Barcode.FORMAT_AZTEC
    ).build()

    var scanner=BarcodeScanning.getClient(options)

    //process the image
    scanner.process(this.inputImage).addOnSuccessListener {
        //loop through the barcodes

        for(i:Barcode in it){
            var valueTYpe=i.valueType
            this.textView.text=valueTYpe.toString()


        }
    }.addOnFailureListener{
        Toast.makeText(this@MainActivity,it.localizedMessage,Toast.LENGTH_SHORT).show()
    }

}

}