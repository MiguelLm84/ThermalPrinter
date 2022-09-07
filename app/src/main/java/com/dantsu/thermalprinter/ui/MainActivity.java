package com.dantsu.thermalprinter.ui;

import static com.dantsu.thermalprinter.ui.ConfigActivity.IP;
import static com.dantsu.thermalprinter.ui.ConfigActivity.PORT;
import static com.dantsu.thermalprinter.ui.ConfigActivity.PREFERENCES_DATA;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.dantsu.thermalprinter.R;
import com.dantsu.thermalprinter.async.AsyncBluetoothEscPosPrint;
import com.dantsu.thermalprinter.async.AsyncEscPosPrint;
import com.dantsu.thermalprinter.async.AsyncEscPosPrinter;
import com.dantsu.thermalprinter.async.AsyncTcpEscPosPrint;
import com.dantsu.thermalprinter.async.AsyncUsbEscPosPrint;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    //BLUETOOTH CONSTANTS
    public static final int PERMISSION_BLUETOOTH = 1;
    public static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    public static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    public static final int PERMISSION_BLUETOOTH_SCAN = 4;

    //USB CONSTANT
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private BluetoothConnection selectedDevice;

    //BUTTONS
    Button btnCarniceria, btnCharcuteria, btnPescaderia;

    //BUNDLE
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dispenser);

        init();
        buttonsEvents();

        /*Button button = (Button) this.findViewById(R.id.button_bluetooth_browse);
        button.setOnClickListener(view -> browseBluetoothDevice());
        button = (Button) findViewById(R.id.button_bluetooth);
        button.setOnClickListener(view -> printBluetooth());
        button = (Button) this.findViewById(R.id.button_usb);
        button.setOnClickListener(view -> printUsb());
        button = (Button) this.findViewById(R.id.button_tcp);
        button.setOnClickListener(view -> printTcp());*/
    }

    private void init() {

        btnCarniceria = this.findViewById(R.id.btnCarniceria);
        btnCharcuteria = this.findViewById(R.id.btnCharcuteria);
        btnPescaderia = this.findViewById(R.id.btnPescaderia);

        bundle = getIntent().getExtras();
    }

    private String getIpArgs() {

        if(bundle == null) {
            return "";
        }
        return bundle.getString(IP);
    }

    private String getPortArgs() {

        if(bundle == null) {
            return "";
        }
        return bundle.getString(PORT);
    }

    private void buttonsEvents() {

        btnCarniceria.setOnClickListener(v -> printTcp("CARNICERIA"));
        btnCharcuteria.setOnClickListener(v -> printTcp("CHARCUTERIA"));
        btnPescaderia.setOnClickListener(v -> printTcp("PESCADERIA"));
    }


    /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                case MainActivity.PERMISSION_BLUETOOTH_ADMIN:
                case MainActivity.PERMISSION_BLUETOOTH_CONNECT:
                case MainActivity.PERMISSION_BLUETOOTH_SCAN:
                    this.printBluetooth();
                    break;
            }
        }
    }

    public void browseBluetoothDevice() {
        final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();

        if (bluetoothDevicesList != null) {
            final String[] items = new String[bluetoothDevicesList.length + 1];
            items[0] = "Default printer";
            int i = 0;
            for (BluetoothConnection device : bluetoothDevicesList) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                items[++i] = device.getDevice().getName();
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Bluetooth printer selection");
            alertDialog.setItems(items, (dialogInterface, i1) -> {
                int index = i1 - 1;
                if (index == -1) {
                    selectedDevice = null;
                } else {
                    selectedDevice = bluetoothDevicesList[index];
                }
                Button button = (Button) findViewById(R.id.button_bluetooth_browse);
                button.setText(items[i1]);
            });

            AlertDialog alert = alertDialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
    }

    public void printBluetooth() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, MainActivity.PERMISSION_BLUETOOTH_ADMIN);

        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MainActivity.PERMISSION_BLUETOOTH_CONNECT);

        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, MainActivity.PERMISSION_BLUETOOTH_SCAN);

        } else {
            new AsyncBluetoothEscPosPrint(
                this,
                new AsyncEscPosPrint.OnPrintFinished() {
                    @Override
                    public void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException) {
                        Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !");
                    }

                    @Override
                    public void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter) {
                        Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !");
                    }
                }
            ).execute(this.getAsyncEscPosPrinter(selectedDevice, ""));
        }
    }

    /*==============================================================================================
    ===========================================USB PART=============================================
    ==============================================================================================*/

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (MainActivity.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {
                            new AsyncUsbEscPosPrint(context,
                                new AsyncEscPosPrint.OnPrintFinished() {
                                    @Override
                                    public void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException) {
                                        Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !");
                                    }

                                    @Override
                                    public void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter) {
                                        Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !");
                                    }
                                }
                            ).execute(getAsyncEscPosPrinter(new UsbConnection(usbManager, usbDevice), ""));
                        }
                    }
                }
            }
        }
    };

    public void printUsb() {

        UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(this);
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);

        if (usbConnection == null || usbManager == null) {
            new AlertDialog.Builder(this)
                .setTitle("USB Connection")
                .setMessage("No USB printer found.")
                .show();
            return;
        }

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0,
            new Intent(MainActivity.ACTION_USB_PERMISSION),
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0);

        IntentFilter filter = new IntentFilter(MainActivity.ACTION_USB_PERMISSION);
        registerReceiver(this.usbReceiver, filter);
        usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
    }

    /*==============================================================================================
    =========================================TCP PART===============================================
    ==============================================================================================*/

    public void printTcp(String optionSelected) {

        //final EditText ipAddress = (EditText) this.findViewById(R.id.edittext_tcp_ip);
        //final EditText portAddress = (EditText) this.findViewById(R.id.edittext_tcp_port);

        try {
            new AsyncTcpEscPosPrint(this, new AsyncEscPosPrint.OnPrintFinished() {
                @Override
                public void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException) {
                    Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !");
                }

                @Override
                public void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter) {
                    Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !");
                }
            }).execute(this.getAsyncEscPosPrinter(new TcpConnection(getIpArgs(),
                            Integer.parseInt(getPortArgs())), optionSelected));

        } catch (NumberFormatException e) {
            new AlertDialog.Builder(this)
                .setTitle("Invalid TCP port address")
                .setMessage("Port field must be an integer")
                .show();
            e.printStackTrace();
        }
    }

    /*==============================================================================================
    ===================================ESC/POS PRINTER PART=========================================
    ==============================================================================================*/

    /**
     * Asynchronous printing
     */
    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection, String optionSelected) {

        //String optionSelected = "";
        SimpleDateFormat format = new SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss");
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);

        return printer.addTextToPrint(
            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n" +
                "[L]\n" +
                "[C]<b><font size='big-6'>01</font></b>\n" +
                "[L]\n" +
                "[C]<font size='big-3'>" + optionSelected + "</font>\n" +
                "[C]<u type='double'>" + format.format(new Date()) + "</u>\n" +
                "[C]\n" +
                "[C]================================\n" +
                "[L]\n" +
                "[C]<font size='big-2'>Espere hasta que su turno aparezca en la pantalla</font>\n" +
                "[L]\n" +
                "[C]================================\n" +
                "[L]\n"
        );
    }

    /*private String getIp() {

        SharedPreferences preferences = this.getSharedPreferences(PREFERENCES_DATA, MODE_PRIVATE);

        return preferences.getString(IP, "");
    }

    private String getPort() {

        SharedPreferences preferences = this.getSharedPreferences(PREFERENCES_DATA, MODE_PRIVATE);

        return preferences.getString(PORT, "");
    }*/
}
