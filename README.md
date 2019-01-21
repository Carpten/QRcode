# Qrcode
HOW TO USE
1.implementation 'com.google.zxing:core:3.3.3'
  implementation 'com.github.Carpten:Qrcode:1.0.0'
2.<uses-permission android:name="android.permission.CAMERA" />
3.<fragment
        android:id="@+id/qr_fragment"
        android:name="com.ysq.qrlib.QrFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent" /> 
