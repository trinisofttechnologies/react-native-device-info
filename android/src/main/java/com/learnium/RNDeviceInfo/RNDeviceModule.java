package com.learnium.RNDeviceInfo;

import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings.Secure;

import com.google.android.gms.iid.InstanceID;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;

import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;


public class RNDeviceModule extends ReactContextBaseJavaModule {

  ReactApplicationContext reactContext;

  public RNDeviceModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNDeviceInfo";
  }

  private String getCurrentLanguage() {
      Locale current = getReactApplicationContext().getResources().getConfiguration().locale;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          return current.toLanguageTag();
      } else {
          StringBuilder builder = new StringBuilder();
          builder.append(current.getLanguage());
          if (current.getCountry() != null) {
              builder.append("-");
              builder.append(current.getCountry());
          }
          return builder.toString();
      }
  }

  private String getCurrentCountry() {
    Locale current = getReactApplicationContext().getResources().getConfiguration().locale;
    return current.getCountry();
  }

  private Boolean isEmulator() {
    return Build.FINGERPRINT.startsWith("generic")
      || Build.FINGERPRINT.startsWith("unknown")
      || Build.MODEL.contains("google_sdk")
      || Build.MODEL.contains("Emulator")
      || Build.MODEL.contains("Android SDK built for x86")
      || Build.MANUFACTURER.contains("Genymotion")
      || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
      || "google_sdk".equals(Build.PRODUCT);
  }

  private Boolean isTablet() {
    int layout = getReactApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
    return layout == Configuration.SCREENLAYOUT_SIZE_LARGE || layout == Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  @ReactMethod
  public void isPinOrFingerprintSet(Callback callback) {
    KeyguardManager keyguardManager = (KeyguardManager) this.reactContext.getSystemService(Context.KEYGUARD_SERVICE); //api 16+
    callback.invoke(keyguardManager.isKeyguardSecure());
  }

  @Override
  public @Nullable Map<String, Object> getConstants() {
    HashMap<String, Object> constants = new HashMap<String, Object>();

    PackageManager packageManager = this.reactContext.getPackageManager();
    String packageName = this.reactContext.getPackageName();

    constants.put("appVersion", "not available");
    constants.put("buildVersion", "not available");
    constants.put("buildNumber", 0);

    try {
      PackageInfo info = packageManager.getPackageInfo(packageName, 0);
      constants.put("appVersion", info.versionName);
      constants.put("buildNumber", info.versionCode);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    String deviceName = "Unknown";

    try {
      BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
      if(myDevice!=null){
        deviceName = myDevice.getName();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }

    constants.put("instanceId", InstanceID.getInstance(this.reactContext).getId());
    constants.put("deviceName", deviceName);
    constants.put("systemName", "Android");
    constants.put("systemVersion", Build.VERSION.RELEASE);
    constants.put("model", Build.MODEL);
    constants.put("brand", Build.BRAND);
    constants.put("deviceId", Build.BOARD);
    constants.put("deviceLocale", this.getCurrentLanguage());
    constants.put("deviceCountry", this.getCurrentCountry());
    constants.put("uniqueId", Secure.getString(this.reactContext.getContentResolver(), Secure.ANDROID_ID));
    constants.put("systemManufacturer", Build.MANUFACTURER);
    constants.put("bundleId", packageName);
    constants.put("userAgent", System.getProperty("http.agent"));
    constants.put("timezone", TimeZone.getDefault().getID());
    constants.put("isEmulator", this.isEmulator());
    constants.put("isTablet", this.isTablet());
    constants.put("density", this.getDensity(this.reactContext));
    constants.put("carrier", this.getCarrier(this.reactContext));
    return constants;
  }
  /**
   * Maps the current display density to a string constant.
   * @param context context to use to retrieve the current display metrics
   * @return a string constant representing the current display density, or the
   *         empty string if the density is unknown
   */
  static String getDensity(final Context context) {
      String densityStr = "";
      final int density = context.getResources().getDisplayMetrics().densityDpi;
      switch (density) {
          case DisplayMetrics.DENSITY_LOW:
              densityStr = "LDPI";
              break;
          case DisplayMetrics.DENSITY_MEDIUM:
              densityStr = "MDPI";
              break;
          case DisplayMetrics.DENSITY_TV:
              densityStr = "TVDPI";
              break;
          case DisplayMetrics.DENSITY_HIGH:
              densityStr = "HDPI";
              break;
          //todo uncomment in android sdk 25
          //case DisplayMetrics.DENSITY_260:
          //    densityStr = "XHDPI";
          //    break;
          case DisplayMetrics.DENSITY_280:
              densityStr = "XHDPI";
              break;
          //todo uncomment in android sdk 25
          //case DisplayMetrics.DENSITY_300:
          //    densityStr = "XHDPI";
          //    break;
          case DisplayMetrics.DENSITY_XHIGH:
              densityStr = "XHDPI";
              break;
          //todo uncomment in android sdk 25
          //case DisplayMetrics.DENSITY_340:
          //    densityStr = "XXHDPI";
          //    break;
          case DisplayMetrics.DENSITY_360:
              densityStr = "XXHDPI";
              break;
          case DisplayMetrics.DENSITY_400:
              densityStr = "XXHDPI";
              break;
          case DisplayMetrics.DENSITY_420:
              densityStr = "XXHDPI";
              break;
          case DisplayMetrics.DENSITY_XXHIGH:
              densityStr = "XXHDPI";
              break;
          case DisplayMetrics.DENSITY_560:
              densityStr = "XXXHDPI";
              break;
          case DisplayMetrics.DENSITY_XXXHIGH:
              densityStr = "XXXHDPI";
              break;
          default:
              densityStr = "other";
              break;
      }
      return densityStr;
  }

  /**
   * Returns the display name of the current network operator from the
   * TelephonyManager from the specified context.
   * @param context context to use to retrieve the TelephonyManager from
   * @return the display name of the current network operator, or the empty
   *         string if it cannot be accessed or determined
   */
  static String getCarrier(final Context context) {
      String carrier = "";
      final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      if (manager != null) {
          carrier = manager.getNetworkOperatorName();
      }
      if (carrier == null || carrier.length() == 0) {
          carrier = "";
      }
      return carrier;
  }
}
