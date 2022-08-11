package Scanner

//class GoogleBarCode:ImageAnalysis.Analyzer {
//    private var options=null
//    private var scanner=null
//    private fun configureBarcode(){
//        //Configuring the type of the barcodes that you want to use.
//        options=BarcodesScannerOptions.Builder().setBarcodeFormats(
//            Barcode.FORMAT_QR_CODE,
//            Barcode.FORMAT_AZTEC).build()
//
//        scanner=BarcodeScanning.getClient(options)
//
//
//    }
//
//    //process the image to get a high level string like representation of the image.
//    private fun processImage():String{
//        scanner.process(image)
//            .addOnSuccessListener{
//                barcodes->
//
//                return barcodes
//                //do something upon task completion
//            }
//            .addOnFailureListener{
//                failure->
//
//                return failure.localizedMessage.toString()
//            }
//
//
//
//    }
//
//    private fun getInformation(barcodes:Barcode){
//
//        for(barcode in barcodes){
//            val bounds=barcode.boundingBox
//            val corners=barcodes.cornerPoints
//            val rawValue=barcode.rawValue
//            val valueType=barcode.valueType
//
//            when(valueType){
//                //if the barcode is of type wifi
//                Barcode.TYPE_WIFI->{
//                    // get the ssid, password and the encryption type of the barcode.
//                }
//                //if the barcode is of type url
//                Barcode.TYPE_URL->{
//                    //get the url title and the url addresss.
//                }
//
//            }
//        }
//
//    }
//
//}