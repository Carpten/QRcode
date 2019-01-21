# Qrcode
# HOW TO USE
1.添加依赖
```implementation 'com.google.zxing:core:3.3.3'
   implementation 'com.github.Carpten:Qrcode:1.0.0'
```
2.添加权限
```<uses-permission android:name="android.permission.CAMERA" />
```
3.xml中引入QrFragment即可
```<fragment
        android:id="@+id/qr_fragment"
        android:name="com.ysq.qrlib.QrFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
