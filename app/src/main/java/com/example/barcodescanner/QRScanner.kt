package com.example.barcodescanner

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


class QRScanner : AppCompatActivity(){
    //The camera and gallery launchers.
    private lateinit var CameraLauncher:ActivityResultLauncher<Intent>;
    private lateinit var GalleryLauncher:ActivityResultLauncher<Intent>;
    private lateinit var scan_button: Button;
    private lateinit var gen_button:Button;
    private lateinit var txt_display:TextView;
    private final var requestedCode=101;
    private lateinit var cameraDisplay: ImageView;

    private lateinit var inputImage:InputImage
    private lateinit var barcodeScanner: BarcodeScanner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscanner)

        this.txt_display=findViewById(R.id.results_txt)

        //Buttons to scan and generate the texts.
        this.scan_button=findViewById(R.id.click_txt_scan)
        this.gen_button=findViewById(R.id.click_txt_gen)

        //adding an event listener.

        this.scan_button.setOnClickListener {

        }



        //Camera Launcher.
        CameraLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult?) {
                    val data=result?.data

                    try{
                        val photo=data?.extras?.get("data") as? Bitmap
                        inputImage= InputImage.fromBitmap(photo!!,0)
                        processQr()

                    }
                    catch(e:Exception){//
                        e.let {
                            Toast.makeText(this@QRScanner,it?.localizedMessage?.toString(),
                                Toast.LENGTH_LONG).show()
                        }

                    } } })


        //Gallery Launcher.
        GalleryLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult?) {
                    val data=result?.data
                    //making a toast for the data.

                    try{
                        val photo=data?.extras?.get("data") as? Bitmap
                        inputImage= InputImage.fromFilePath(this@QRScanner,data?.data!!)
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

    }

    //This is a function to process the QR code.
    private fun processQr(){

        barcodeScanner.process(inputImage!!).addOnSuccessListener {
            //handle the barcode list
            for(barcode: Barcode in it){
                val valueType=barcode.valueType
                when(valueType){
                    Barcode.TYPE_WIFI ->{
                        val ssid=barcode.wifi!!.password
                        val password=barcode.wifi!!.password
                        val type=barcode.wifi!!.encryptionType
                        this.txt_display.text="$ssid, $password, $type"

                    }

                    Barcode.TYPE_URL ->{
                        val title=barcode.url!!.title
                        val url=barcode.url!!.url

                        //start an activity to open the barcode of type url.
                        Uri.parse(url).let{
                                url->
                            var intent= Intent.createChooser(
                                Intent(Intent.ACTION_VIEW, url),
                                "Perform Action Using"
                            )
                            startActivity(intent)
                        }
                    }
                    Barcode.TYPE_PHONE->{
                        var phoneNo= Uri.parse(barcode.phone.toString()).let{
                                uri->
                            startActivity(Intent(Intent.ACTION_DIAL,uri))
                        }

                    }

                    Barcode.TYPE_TEXT ->{
                        val data=barcode.displayValue
                        this.txt_display.text="data: $data"

                    }

                    Barcode.FORMAT_QR_CODE->{
                        val data=barcode.valueType
                        this.txt_display.text="Hello"

                    }

                    Barcode.TYPE_PRODUCT->{
                        var desc=barcode.displayValue
                        this.txt_display.text=desc

                    }

                }
            }
        }.addOnFailureListener{
                err->
            Toast.makeText(this@QRScanner,err.localizedMessage.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    //Check the users permissions.
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
}