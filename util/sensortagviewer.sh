export ENABLE_GLUON_COMMERCIAL_EXTENSIONS=true
export JAVA_HOME=/home/pi/jdk-11.0.13
export PATH=$JAVA_HOME/bin:$PATH


java --illegal-access=permit -Dmonocle.platform=EGL -Dembedded=monocle -Dglass.platform=Monocle -Degl.displayid=/dev/dri/card0  -Dprism.lcdtext=false -Dmonocle.egl.lib=/home/pi/javafx-sdk-18/lib/libgluon_drm-1.1.6.so --module-path /home/pi/javafx-sdk-18/lib   -Dprism.verbose=true  -Djavafx.verbose=true -Dcom.sun.javafx.isEmbedded=true -Dcom.sun.javafx.touch=true -Dcom.sun.javafx.virtualKeyboard=javafx   --add-modules javafx.controls,javafx.graphics --add-opens javafx.graphics/com.sun.javafx.util=ALL-UNNAMED --add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED --add-opens javafx.base/com.sun.javafx.beans=ALL-UNNAMED -jar /home/pi/blessed-javafx-rpi3-0.0.1-SNAPSHOT.jar
