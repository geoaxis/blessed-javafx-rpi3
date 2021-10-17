# blessed-javafx-rpi3

A JavaFX based application that uses BLE stack of raspberry pi3

# Setup
- Install Raspberry pi OS on your raspberry pi 3, 32 bit (should also work on 4 and on 64 bit version too bue I have not tested it yet)
- Make sure bluetooth packages are installed and your user (pi) is in the bluetooth group
- Check that your bluetooth device is available
    ```
    pi@raspberrypi:~ $ hciconfig
    hci0:   Type: Primary  Bus: UART
            BD Address: B8:27:EB:3F:C4:12  ACL MTU: 1021:8  SCO MTU: 64:1
            UP RUNNING
            RX bytes:9180 acl:0 sco:0 events:375 errors:0
            TX bytes:2215 acl:0 sco:0 commands:137 errors:0
    ```
- Check the status of bluetooth and hciuart services (they should both be running)
    ```
    pi@raspberrypi:~ $ service bluetooth status
    ● bluetooth.service - Bluetooth service
       Loaded: loaded (/lib/systemd/system/bluetooth.service; enabled; vendor preset: enabled)
       Active: active (running) since Fri 2021-10-15 10:03:41 BST; 1 day 13h ago
         Docs: man:bluetoothd(8)
     Main PID: 473 (bluetoothd)
       Status: "Running"
        Tasks: 1 (limit: 409)
       CGroup: /system.slice/bluetooth.service
               └─473 /usr/lib/bluetooth/bluetoothd
    
    Oct 15 10:03:41 raspberrypi systemd[1]: Starting Bluetooth service...
    Oct 15 10:03:41 raspberrypi bluetoothd[473]: Bluetooth daemon 5.50
    Oct 15 10:03:41 raspberrypi systemd[1]: Started Bluetooth service.
    Oct 15 10:03:42 raspberrypi bluetoothd[473]: Bluetooth management interface 1.18 initialized
    Oct 15 10:03:42 raspberrypi bluetoothd[473]: Sap driver initialization failed.
    Oct 15 10:03:42 raspberrypi bluetoothd[473]: sap-server: Operation not permitted (1)
    Oct 15 10:03:42 raspberrypi bluetoothd[473]: Failed to set mode: Rejected (0x0b)
    Oct 15 10:03:42 raspberrypi bluetoothd[473]: Failed to set mode: Rejected (0x0b)
    Oct 15 10:03:42 raspberrypi bluetoothd[473]: Failed to set privacy: Rejected (0x0b)
    pi@raspberrypi:~ $ service hciuart status
    ● hciuart.service - Configure Bluetooth Modems connected by UART
       Loaded: loaded (/lib/systemd/system/hciuart.service; enabled; vendor preset: enabled)
       Active: active (running) since Fri 2021-10-15 10:03:41 BST; 1 day 13h ago
     Main PID: 459 (hciattach)
        Tasks: 1 (limit: 409)
       CGroup: /system.slice/hciuart.service
               └─459 /usr/bin/hciattach /dev/serial1 bcm43xx 3000000 flow - b8:27:eb:3f:c4:12
    
    Oct 15 10:03:34 raspberrypi systemd[1]: Starting Configure Bluetooth Modems connected by UART...
    Oct 15 10:03:41 raspberrypi btuart[350]: bcm43xx_init
    Oct 15 10:03:41 raspberrypi btuart[350]: Flash firmware /lib/firmware/brcm/BCM4345C0.hcd
    Oct 15 10:03:41 raspberrypi btuart[350]: Set BDADDR UART: b8:27:eb:3f:c4:12
    Oct 15 10:03:41 raspberrypi btuart[350]: Set Controller UART speed to 3000000 bit/s
    Oct 15 10:03:41 raspberrypi btuart[350]: Device setup complete
    Oct 15 10:03:41 raspberrypi systemd[1]: Started Configure Bluetooth Modems connected by UART.
    ```
- In the case you have hciuart service errors and if you have an LCD like JoyIT 5-inch touch screen it may add config similar to `console=ttyAMA0,115200`. You will need to remove it (so that hciuart service can work and bluetooth service is enabled)
- While not necessary, you may like to only enable LE part of bluetooth for Bluez by adding `ControllerMode = le` to `/etc/bluetooth/main.conf`
- It may be necessary to enable fake kms for 3d to work. TODO


  -Djava.library.path=/opt/javafx-sdk-17.0.0.1/lib/ --module-path=/opt/javafx-sdk-17.0.0.1/lib/ --add-modules=javafx.controls,javafx.graphics,javafx.fxml -Dglass.platform=Monocle  -Dmonocle.platform=EGL -Dembedded=monocle -Dmonocle.egl.lib=/opt/javafx-sdk-17.0.0.1/lib/libgluon_drm_debug-1.1.6.so -Dglass.platform=Monocle -Degl.displayid=/dev/dri/card0  -Duse.egl=true -Dcom.sun.javafx.isEmbedded=true