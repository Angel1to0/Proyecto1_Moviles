package com.example.proyecto1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

const val REQUEST_ENABLE_BT = 1;

/**Variable que verifica si el dispositivo posee comunicacion bluetooth*/
class MainActivity : AppCompatActivity() {

    //Variables para comunicacion Bluetooth
    /**Bluetooth Adapter*/
    lateinit var mBtAdapter: BluetoothAdapter // Con los ' : ', especificamos que es de tipo BluetoothAdapter
    var mAddressDevice: ArrayAdapter<String>? = null //Se guardan las direcciones de los dispositivos bluetooth
    var mNameDevices: ArrayAdapter<String>? = null //Se guardan los nombres de los dispositivos bluetooth

    companion object{
        var m_myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //Con el ' = ', asignamos el identificador bluetooth del HC-05 o HC-06 a la variable
        private var m_bluetoothSocket: BluetoothSocket? = null //generamos una variable bluetooth socket y le damos un valor nulo

        var m_isConnected: Boolean = false
        lateinit var m_address: String //generamos una variable de tipo String, que le asignamremos un valor despues, por eso ocupamos lateinit
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAddressDevice = ArrayAdapter(this,android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this,android.R.layout.simple_list_item_1)

        val jbtOnBT = findViewById<Button>(R.id.idBtnOnBT)
        val jbtOffBT = findViewById<Button>(R.id.idBtnOffBT)
        val jbtConnect = findViewById<Button>(R.id.idBtnConect)

        val jbtLuzOn = findViewById<Button>(R.id.idBtnLuz_1on)
        val jbtLuzOff = findViewById<Button>(R.id.idBtnLuz_1off)

        val jbtLuz2On = findViewById<Button>(R.id.idBtnLuz_2on)
        val jbtLuz2Off = findViewById<Button>(R.id.idBtnLuz_2off)

        val jbtSOSon = findViewById<Button>(R.id.idBtnEnviar)
        val jbtSOSoff = findViewById<Button>(R.id.idBtnDesactivar)

        val jbtDispBT = findViewById<Button>(R.id.idBtnDispBT)
        val jbtSpinDisp = findViewById<Spinner>(R.id.idSpinDisp)

        val imledR = findViewById<ImageView>(R.id.imledr)
        imledR.setBackgroundResource(R.drawable.ledapagado)

        val imledA = findViewById<ImageView>(R.id.imleda)
        imledA.setBackgroundResource(R.drawable.ledapagado)

        val imSos = findViewById<ImageView>(R.id.ximSOS)
        imSos.setBackgroundResource(R.drawable.sosapagado)

        /** ----------------------------------------------------------------------------- */

        //Cuando se inicia la App entra aqui
        val someActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ){result ->
            if (result.resultCode == REQUEST_ENABLE_BT){
                Log.i("MainActivity","ACTIVIDAD REGISTRADA")
            }
        }

        /**Inicializacion del Bluetooth Adapter*/
        mBtAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        /**Checar si esta encendido o apagado*/
        if (mBtAdapter == null){
            Toast.makeText(this,"Bluetooth no esta disponible en este dispositivo",Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this,"Bluetooth esta disponible en este dispositivo", Toast.LENGTH_LONG).show()
        }

        /** --------------------------------------------------------------------------------------- */

        /**Boton Encender bluetooth*/
        jbtOnBT.setOnClickListener{
            if (mBtAdapter.isEnabled){
                //Si ya esta activado
                Toast.makeText(this,"El Bluetooth ya esta activado", Toast.LENGTH_LONG).show()
            }else{
                //Encender Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,Manifest.permission.BLUETOOTH_CONNECT
                )!=PackageManager.PERMISSION_GRANTED
                ){
                    Log.i("MainActivity","ActivityCompat#requestPermissions")
                }
                someActivityResultLauncher.launch(enableBtIntent)
            }
        }

        /**Boton apagar Bluetooth*/
        jbtOffBT.setOnClickListener{
            if(!mBtAdapter.isEnabled){
                //Si ya esta desactivado
                Toast.makeText(this,"Bluetooth ya se encuentra desactivado",Toast.LENGTH_LONG).show()
            }else{
                //Encender Bluetooth
                mBtAdapter.disable()
                Toast.makeText(this,"Se ha desactivado el bluetooth",Toast.LENGTH_LONG).show()
            }
        }

        /**Boton dispositivos emparejados*/
        jbtDispBT.setOnClickListener{
            if(mBtAdapter.isEnabled){
                //procedo a buscar a todos los dispositivos que han sido vinculados con el telefono
                val pairedDevices: Set<BluetoothDevice>? = mBtAdapter?.bondedDevices
                //Se cargan las direcciones de un dispositivo
                mAddressDevice!!.clear()
                //nombre de un dispositivo
                mNameDevices!!.clear()

                //Separo el nombre y la direccion del dispositivo
                pairedDevices?.forEach{device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address
                    mAddressDevice!!.add(deviceHardwareAddress)
                    mNameDevices!!.add(deviceName)
                }

                //Actualizo los dispositivos
                jbtSpinDisp.setAdapter(mNameDevices)
            }else{
                val noDevices = "Ningun dispositivo pudo ser emparejado"
                mAddressDevice!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(this,"Primero vincule un dispositivo Bluetooth",Toast.LENGTH_LONG).show()
            }
        }

        /**Boton Conectar*/
        jbtConnect.setOnClickListener{
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    val IntValSpin = jbtSpinDisp.selectedItemPosition
                    m_address = mAddressDevice!!.getItem(IntValSpin).toString()
                    Toast.makeText(this, m_address, Toast.LENGTH_LONG).show()
                    mBtAdapter?.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                    //creamos un socket bluetooth, que permite intercambiar informacion
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                }

                Toast.makeText(this,"CONEXION EXITOSA",Toast.LENGTH_LONG).show()
                Log.i("MainActivity","CONEXION EXITOSA")
            }catch (e: IOException){
                e.printStackTrace()
                Toast.makeText(this,"ERROR DE CONEXION",Toast.LENGTH_LONG).show()
                Log.i("MainActivity","ERROR DE CONEXION")
            }
        }

    jbtLuzOn.setOnClickListener{
            imledR.setBackgroundResource(R.drawable.ledrojo)
            sendCommand("A")
        }

        jbtLuzOff.setOnClickListener{
            imledR.setBackgroundResource(R.drawable.ledapagado)
            sendCommand("B")
        }

        jbtLuz2On.setOnClickListener{
            imledA.setBackgroundResource(R.drawable.ledamarillo)
            sendCommand("C")
        }

        jbtLuz2Off.setOnClickListener{
            sendCommand("D")
            imledA.setBackgroundResource(R.drawable.ledapagado)
        }

        jbtSOSon.setOnClickListener{
            imSos.setBackgroundResource(R.drawable.sosencendido)
            sendCommand2("E")
        }

        jbtSOSoff.setOnClickListener{
            sendCommand("F")
            imSos.setBackgroundResource(R.drawable.sosapagado)
        }
    }

    private fun sendCommand(input: String) {
        if(m_bluetoothSocket != null){
            try {
                /**Por medio del Socket, lo que se envia es la letra, en forma de Bytes*/
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }


    private var bluetoothThread: BluetoothThread? = null

    private fun sendCommand2(input: String) {
        if (m_bluetoothSocket != null) {
            try {
                // Inicializar BluetoothThread
                bluetoothThread = BluetoothThread(m_bluetoothSocket!!)
                bluetoothThread?.start()

                // Enviar comando al dispositivo Bluetooth
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())

                // Esperar respuesta del dispositivo Bluetooth
                bluetoothThread?.setBluetoothResponseListener(object : BluetoothThread.BluetoothResponseListener {
                    override fun onResponseReceived(response: String) {
                        if (response == "E") {
                            runOnUiThread {
                                // Mostrar Toast de señal recibida
                                Toast.makeText(this@MainActivity, "Señal Recibida", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Enviar otro comando para seguir recibiendo señales
                            m_bluetoothSocket!!.outputStream.write("Señal detenida".toByteArray())
                        }

                        // Detener BluetoothThread
                        if (response != "E") {
                            bluetoothThread?.stopThread()
                            bluetoothThread = null
                        }
                    }
                })
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Clase para manejar la lectura continua de la respuesta del dispositivo Bluetooth en un hilo separado
    private class BluetoothThread(private val bluetoothSocket: BluetoothSocket) : Thread() {
        private var responseListener: BluetoothResponseListener? = null
        private var running = true

        override fun run() {
            val buffer = ByteArray(1024)

            while (running) {
                try {
                    val bytesRead = bluetoothSocket.inputStream.read(buffer)
                    val message = String(buffer, 0, bytesRead)

                    // Llamar a BluetoothResponseListener para notificar que se recibió la respuesta
                    responseListener?.onResponseReceived(message)
                } catch (e: IOException) {
                    e.printStackTrace()
                    running = false
                }
            }
        }

        fun setBluetoothResponseListener(listener: BluetoothResponseListener) {
            responseListener = listener
        }

        fun stopThread() {
            running = false
        }

        // Interface para notificar cuando se recibe una respuesta del dispositivo Bluetooth
        interface BluetoothResponseListener {
            fun onResponseReceived(response: String)
        }
    }

}