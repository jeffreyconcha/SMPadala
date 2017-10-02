package com.codepan.utils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.codepan.R;
import com.codepan.database.SQLiteAdapter;
import com.codepan.model.GpsObj;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CustomTypefaceSpan;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class CodePanUtils {

	public static void cancelAlarm(Context context, Class<?> receiver, int requestCode) {
		Intent intent = new Intent(context, receiver);
		PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pi);
	}

	public static void closePane(Context context, final View view, int id) {
		Animation anim = AnimationUtils.loadAnimation(context, id);
		view.setAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
		view.startAnimation(anim);
	}

	public static Drawable bitmapToDrawable(Context context, Bitmap bitmap) {
		return new BitmapDrawable(context.getResources(), bitmap);
	}

	public static long dateToMillis(String date) {
		return dateTimeToMillis(date, "00:00:00");
	}

	public static long timeToMillis(String time) {
		return dateTimeToMillis("0000-00-00", time);
	}

	public static long dateTimeToMillis(String date, String time) {
		long millis = 0;
		if(date == null || time == null || date.isEmpty() || time.isEmpty()) {
			return millis;
		}
		String dateArray[] = date.split("\\-");
		String timeArray[] = time.split("\\:");
		int year = Integer.parseInt(dateArray[0]);
		int month = Integer.parseInt(dateArray[1]);
		int day = Integer.parseInt(dateArray[2]);
		int hour = Integer.parseInt(timeArray[0]);
		int min = Integer.parseInt(timeArray[1]);
		int sec = Integer.parseInt(timeArray[2]);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day, hour, min, sec);
		millis = cal.getTimeInMillis();
		return millis;
	}

	public static String millisToHours(long millis) {
		String total = null;
		final int spm = 60;
		final int mph = 60;
		final int mps = 1000;
		long sec = millis / mps;
		total = sec + "s";
		if(sec > spm) {
			long secExcess = sec % spm;
			long min = sec / spm;
			total = min + "m " + secExcess + "s";
			if(min > mph) {
				long minExcess = min % mph;
				long hours = min / mph;
				total = hours + "h " + minExcess + "m";
			}
		}
		return total;
	}

	public static String getNormalTime(String militaryTime, boolean hasSecond) {
		if(militaryTime == null || militaryTime.isEmpty()) {
			return militaryTime;
		}
		String period = null;
		String[] timeArray = militaryTime.split(":");
		int hour_24 = Integer.parseInt(timeArray[0]);
		int hour = 0;
		if(hour_24 > 12) {
			hour = hour_24 - 12;
			period = "PM";
		}
		else if(hour_24 == 0) {
			hour = 12;
			period = "AM";
		}
		else if(hour_24 == 12) {
			hour = 24 - hour_24;
			period = "PM";
		}
		else {
			hour = hour_24;
			period = "AM";
		}
		if(!hasSecond) {
			return hour + ":" + timeArray[1] + " " + period;
		}
		else {
			return hour + ":" + timeArray[1] + ":" +
					timeArray[2] + " " + period;
		}
	}

	public static String minutesToTime(int minutes) {
		int hour = minutes / 60;
		int min = minutes % 60;
		int sec = 0;
		return String.format(Locale.ENGLISH, "%02d", hour) + ":" +
				String.format(Locale.ENGLISH, "%02d", min) + ":" +
				String.format(Locale.ENGLISH, "%02d", sec);
	}


	public static String secondsToTime(int seconds) {
		int hour = seconds / 3600;
		int min = (seconds % 3600) / 60;
		int sec = (seconds % 3600) % 60;
		return String.format(Locale.ENGLISH, "%02d", hour) + ":" +
				String.format(Locale.ENGLISH, "%02d", min) + ":" +
				String.format(Locale.ENGLISH, "%02d", sec);
	}

	public static int timeToMinutes(String militarytime) {
		int totalMinutes = 0;
		if(militarytime == null || militarytime.isEmpty()) {
			return totalMinutes;
		}
		String[] timeArray = militarytime.split(":");
		int timeHour = Integer.parseInt(timeArray[0]);
		int timeMin = Integer.parseInt(timeArray[1]);
		totalMinutes = (timeHour * 60) + timeMin;
		return totalMinutes;
	}

	public static int timeToSeconds(String militarytime) {
		if(militarytime.isEmpty()) {
			return 0;
		}
		String[] timeArray = militarytime.split(":");
		int timeHour = Integer.parseInt(timeArray[0]);
		int timeMin = Integer.parseInt(timeArray[1]);
		int timeSec = Integer.parseInt(timeArray[2]);
		return (((timeHour * 60) + timeMin) * 60) + timeSec;
	}

	public static String unicodeToString(String text) {
		String utf8 = "";
		try {
			byte[] convertToBytes = text.getBytes("UTF-8");
			utf8 = new String(convertToBytes, Charset.forName("UTF-8"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return utf8;
	}

	@SuppressWarnings("resource")
	public static void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			inChannel.close();
			outChannel.close();
		}
	}

	public static NotificationCompat.Builder createNotificationBuilder(Context context, int resID) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(resID);
		builder.setPriority(Notification.PRIORITY_HIGH);
		builder.setOnlyAlertOnce(true);
		builder.setAutoCancel(true);
		return builder;
	}

	public static String createParsableString(ArrayList<String> list, String delimeter) {
		String parsableString = "";
		int position = 0;
		for(String s : list) {
			if(position == list.size() - 1) {
				parsableString = parsableString + s;
			}
			else {
				parsableString = parsableString + s + delimeter;
			}
			position++;
		}
		return parsableString;
	}

	public static boolean decryptTextFile(Context context, String folder, String password) {
		boolean result = false;
		String path = context.getDir(folder, Context.MODE_PRIVATE).getPath();
		File dir = new File(path);
		if(dir.exists() && dir.isDirectory()) {
			String[] child = dir.list();
			if(child.length > 0) {
				for(String file : child) {
					result = file.contains(".") || CodePanUtils.decryptFile(context, folder, file, password, ".txt");
				}
			}
			else {
				result = true;
			}
		}
		else {
			result = true;
		}
		return result;
	}

	public static boolean decryptFile(Context context, String folderName, String fileName, String password, String extFile) {
		boolean result = false;
		String path = context.getDir(folderName, Context.MODE_PRIVATE).getPath() + "/" + fileName;
		if(fileName.contains(extFile)) {
			return true;
		}
		try {
			File file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(path + extFile);
			byte[] key = generateKey(password, 16);
			SecretKeySpec sks = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, sks);
			CipherInputStream cis = new CipherInputStream(fis, cipher);
			int b = 0;
			byte[] d = new byte[8];
			while((b = cis.read(d)) != -1) {
				fos.write(d, 0, b);
			}
			fos.flush();
			fos.close();
			cis.close();
			result = file.delete();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean deleteFile(String path) {
		boolean result = false;
		File file = new File(path);
		if(file.exists() && !file.isDirectory()) {
			result = file.delete();
		}
		return result;
	}

	public static boolean deleteFile(Context context, String folder, String fileName) {
		String path = context.getDir(folder, Context.MODE_PRIVATE).getPath() + "/" + fileName;
		return deleteFile(path);
	}

	public static void deleteFiles(String path) {
		File file = new File(path);
		if(file.exists()) {
			String deleteCmd = "rm -r " + path;
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec(deleteCmd);
			}
			catch(IOException e) {
			}
		}
	}

	public static boolean deleteFilesInDir(Context context, String folder) {
		boolean result = false;
		String path = context.getDir(folder, Context.MODE_PRIVATE).getPath();
		File dir = new File(path);
		if(dir.exists() && dir.isDirectory()) {
			String[] child = dir.list();
			if(child.length > 0) {
				for(String file : child) {
					result = new File(path + "/" + file).delete();
				}
			}
			else {
				result = true;
			}
		}
		else {
			result = true;
		}
		return result;
	}

	public static boolean downloadFile(Context context, String urlLink, String folder,
									   String fileName) {
		boolean result = false;
		File dir = context.getDir(folder, Context.MODE_PRIVATE);
		if(!dir.exists()) {
			dir.mkdir();
		}
		try {
			URL url = new URL(urlLink);
			URLConnection connection = url.openConnection();
			connection.connect();
			//int fileLength = connection.getContentLength();
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(dir + "/" + fileName);
			byte data[] = new byte[1024];
			long total = 0;
			int count;
			while((count = input.read(data)) != -1) {
				total += count;
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
			result = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String encryptString(String text, String password) {
		String encrypted = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			final byte[] ivData = new byte[cipher.getBlockSize()];
			final SecureRandom sr = new SecureRandom();
			sr.nextBytes(ivData);
			IvParameterSpec iv = new IvParameterSpec(ivData);
			byte[] key = generateKey(password, 32);
			SecretKeySpec sks = new SecretKeySpec(key, "AES");
			byte[] inputByte = text.getBytes("UTF-8");
			cipher.init(Cipher.ENCRYPT_MODE, sks, iv);
			encrypted = new String(Base64.encode(cipher.doFinal(inputByte), Base64.DEFAULT));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return encrypted;
	}

	public static boolean encryptFile(Context context, String folderName, String fileName, String password, String extFile) {
		boolean result = false;
		String pathIn = context.getDir(folderName, Context.MODE_PRIVATE).getPath() + "/" + fileName;
		String pathOut = context.getDir(folderName, Context.MODE_PRIVATE).getPath() + "/" + fileName.replace(extFile, "");
		try {
			File file = new File(pathIn);
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(pathOut);
			byte[] key = generateKey(password, 16);
			SecretKeySpec sks = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, sks);
			CipherOutputStream cos = new CipherOutputStream(fos, cipher);
			int b;
			byte[] d = new byte[8];
			while((b = fis.read(d)) != -1) {
				cos.write(d, 0, b);
			}
			cos.flush();
			cos.close();
			fis.close();
			result = file.delete();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void expandView(final View view, final boolean isVertical) {
		view.setVisibility(View.VISIBLE);
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int value = isVertical ? view.getMeasuredHeight() : view.getMeasuredWidth();
		ValueAnimator animator = ValueAnimator.ofInt(0, value);
		animator.setDuration(250);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				int value = (Integer) valueAnimator.getAnimatedValue();
				LayoutParams layoutParams = view.getLayoutParams();
				if(isVertical) {
					layoutParams.height = value;
				}
				else {
					layoutParams.width = value;
				}
				view.setLayoutParams(layoutParams);
			}
		});
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				if(isVertical) {
					view.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				}
				else {
					view.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
				}
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
			}
		});
		animator.start();
	}

	public static void collapseView(final View view, final boolean isVertical) {
		int value = isVertical ? view.getMeasuredHeight() : view.getMeasuredWidth();
		ValueAnimator animator = ValueAnimator.ofInt(value, 0);
		animator.setDuration(250);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				int value = (Integer) valueAnimator.getAnimatedValue();
				LayoutParams layoutParams = view.getLayoutParams();
				if(isVertical) {
					layoutParams.height = value;
				}
				else {
					layoutParams.width = value;
				}
				view.setLayoutParams(layoutParams);
			}
		});
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				view.setVisibility(View.GONE);
				if(isVertical) {
					view.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				}
				else {
					view.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
				}
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
			}
		});
		animator.start();
	}

	public static void slideView(final View view, final boolean up, final long delay) {
		final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
				view.getLayoutParams();
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(up) {
					params.bottomMargin += 10;
					view.setLayoutParams(params);
					if(params.bottomMargin < 0) {
						handler.postDelayed(this, delay);
					}
					else {
						params.bottomMargin = 0;
						view.setLayoutParams(params);
					}
				}
				else {
					int size = -view.getHeight();
					params.bottomMargin -= 10;
					view.setLayoutParams(params);
					if(params.bottomMargin > size) {
						handler.postDelayed(this, delay);
					}
					else {
						params.bottomMargin = size;
						view.setLayoutParams(params);
					}
				}
			}
		}, delay);
	}

	@SuppressWarnings("resource")
	public static boolean extractDatabase(Context context, String folder, String name, boolean external) {
		boolean result = false;
		File dir = null;
		try {
			if(external) {
				String path = Environment.getExternalStorageDirectory() + "/" + folder;
				dir = new File(path);
			}
			else {
				dir = context.getDir(folder, Context.MODE_PRIVATE);
			}
			if(!dir.exists()) {
				dir.mkdir();
			}
			if(dir.canWrite()) {
				String currentDBPath = "//data//" + context.getPackageName() + "//databases//" + name;
				File data = Environment.getDataDirectory();
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(dir, name);
				if(currentDB.exists()) {
					FileChannel src = new FileInputStream(currentDB).getChannel();
					FileChannel dst = new FileOutputStream(backupDB).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
				}
			}
			result = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static byte[] generateKey(String password, int length) {
		byte[] key = null;
		try {
			key = password.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, length);
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}

	public static String getCalendarDate(String date, boolean isAbbreviated, boolean withYear) {
		String calendarDate = "";
		if(date != null && !date.isEmpty()) {
			String[] array = date.split("\\-");
			int year = Integer.parseInt(array[0]);
			int month = Integer.parseInt(array[1]);
			int day = Integer.parseInt(array[2]);
			String nameOfMonths = getNameOfMonths(month, isAbbreviated, false);
			if(withYear) {
				calendarDate = nameOfMonths + " " + day + ", " + year;
			}
			else {
				calendarDate = nameOfMonths + " " + day;
			}
		}
		return calendarDate;
	}

	public static int getBackStackCount(FragmentActivity activity) {
		return activity.getSupportFragmentManager().getBackStackEntryCount();
	}

	public static int getBatteryLevel(Context context) {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent intent = context.getApplicationContext().registerReceiver(null, filter);
		if(intent != null) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			return (level * 100) / scale;
		}
		else {
			return 0;
		}
	}

	public static Bitmap getBitmapImage(Context context, String folderName, String fileName) {
		String path = context.getDir(folderName, Context.MODE_PRIVATE).getPath() + "/" + fileName;
		File image = new File(path);
		return BitmapFactory.decodeFile(image.getAbsolutePath());
	}

	public static boolean getBooleanSharedPref(Context context, String sharedPref, String key) {
		SharedPreferences prefs = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
		return prefs.getBoolean(key, false);
	}

	public static String getDateAfter(String date, int noOfDays) {
		if(date != null) {
			long millis = dateTimeToMillis(date, "00:00:00");
			long output = millis + (noOfDays * 86400000);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(output);
			return String.format(Locale.ENGLISH, "%tF", cal);
		}
		else {
			return null;
		}
	}

	public static Calendar getCalendar(String date) {
		Calendar cal = Calendar.getInstance();
		if(date != null) {
			String array[] = date.split("\\-");
			int year = Integer.parseInt(array[0]);
			int month = Integer.parseInt(array[1]);
			int day = Integer.parseInt(array[2]);
			cal.set(year, month - 1, day);
		}
		return cal;
	}

	public static String getDeviceID(Context context) {
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if(manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
			return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		}
		else {
			return manager.getDeviceId();
		}
	}

	public static HashMap<String, Integer> getPhoneInfo(Context context) {
		HashMap<String, Integer> map = new HashMap<>();
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if(manager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
			String networkOperator = manager.getNetworkOperator();
			int mcc = Integer.parseInt(networkOperator.substring(0, 3));
			int mnc = Integer.parseInt(networkOperator.substring(3));
			map.put("mcc", mcc);
			map.put("mnc", mnc);
			final GsmCellLocation location = (GsmCellLocation) manager.getCellLocation();
			if(location != null) {
				int cid = location.getCid();
				int lac = location.getLac();
				map.put("cid", cid);
				map.put("lac", lac);
			}
		}
		return map;
	}

	public static String getFilePath(Context context, Uri uri) {
		String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		int column_index = 0;
		try {
			column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
		}
		catch(Exception e) {
			return uri.getPath();
		}
		return cursor.getString(column_index);
	}

	public static String doHttpGet(String url, JSONObject paramsObj, int timeOut) {
		String params = "?";
		Iterator<String> iterator = paramsObj.keys();
		try {
			int i = 0;
			while(iterator.hasNext()) {
				String key = iterator.next();
				String value = paramsObj.getString(key);
				String encoded = URLEncoder.encode(value, "UTF-8");
				if(i != 0) {
					params += "&" + key + "=" + encoded;
				}
				else {
					params += key + "=" + encoded;
				}
				i++;
			}
		}
		catch(JSONException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return getHttpResponse(url, params, timeOut, "GET");
	}

	public static String doHttpPost(String url, JSONObject paramsObj, int timeOut) {
		return getHttpResponse(url, paramsObj.toString(), timeOut, "POST");
	}

	private static String getHttpResponse(String host, String json, int timeOut, String method) {
		boolean result = false;
		StringBuilder response = new StringBuilder();
		String exception = null;
		String message = null;
		HttpURLConnection connection = null;
		boolean doOutput = method != null && method.equals("POST");
		String uri = method != null && method.equals("GET") ? host + json : host;
		String contentType = method != null && method.equals("GET") ?
				"application/x-www-form-urlencoded" : "application/json";
		try {
			URL url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(timeOut);
			connection.setReadTimeout(timeOut);
			connection.setRequestMethod(method);
			connection.setRequestProperty("Content-Type", contentType);
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setRequestProperty("connection", "close");
			connection.setRequestProperty("Accept-Encoding", "");
			connection.setDoInput(true);
			connection.setDoOutput(doOutput);
			connection.setUseCaches(false);
			connection.connect();
			Log.e("URL", uri);
			if(method != null && method.equals("POST")) {
				Writer out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
				BufferedWriter writer = new BufferedWriter(out);
				writer.write(json);
				writer.flush();
				writer.close();
			}
			if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
				if(!url.getHost().equals(connection.getURL().getHost())) {
					message = "There was a problem in your internet connection. " +
							"Please check and try again.";
				}
				else {
					Reader in = new InputStreamReader(connection.getInputStream());
					BufferedReader reader = new BufferedReader(in);
					String line;
					while((line = reader.readLine()) != null) {
						response.append(line);
					}
					reader.close();
					result = true;
				}
			}
		}
		catch(SocketTimeoutException ste) {
			ste.printStackTrace();
			exception = ste.toString();
			message = "Connection timed out, the server is taking too long to respond. " +
					"Please check your internet connection and try again.";
		}
		catch(UnknownHostException he) {
			exception = he.toString();
			message = "You are getting weak internet connection. " +
					"Please find a reliable source to continue.";
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
			exception = ioe.toString();
			message = "You are getting weak internet connection. " +
					"Please find a reliable source to continue.";
		}
		catch(Exception e) {
			e.printStackTrace();
			exception = e.toString();
			message = e.getMessage();
		}
		finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
		if(!result) {
			try {
				JSONObject field = new JSONObject();
				JSONObject error = new JSONObject();
				field.put("message", message);
				field.put("exception", exception);
				error.put("error", field);
				response.append(error.toString());
			}
			catch(JSONException je) {
				je.printStackTrace();
			}
		}
		return response.toString();
	}

	public static String uploadFile(String url, String json, String name, File file) {
		String response = null;
		String message = null;
		String exception = null;
		final int INDENT = 4;
		boolean result = false;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		try {
			MultipartEntityBuilder entity = MultipartEntityBuilder.create();
			entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addTextBody("params", json);
			entity.addPart(name, new FileBody(file));
			httppost.setEntity(entity.build());
			HttpResponse httpResponse = httpclient.execute(httppost);
			HttpEntity httpEntity = httpResponse.getEntity();
			response = EntityUtils.toString(httpEntity);
			Log.e("URL", url);
			result = true;
		}
		catch(ClientProtocolException cpe) {
			exception = cpe.toString();
			message = cpe.getMessage();
		}
		catch(IOException ioe) {
			exception = ioe.toString();
			message = "You are getting weak internet connection. " +
					"Please find a reliable source to continue.";
		}
		if(!result) {
			try {
				JSONObject error = new JSONObject();
				JSONObject field = new JSONObject();
				field.put("message", message);
				field.put("exception", exception);
				error.put("error", field);
				response = error.toString(INDENT);
			}
			catch(JSONException je) {
				je.printStackTrace();
			}
		}
		return response;
	}

	public static String uploadFile(String url, String json, String name, String mimeType, File file) {
		String response = null;
		String message = null;
		String exception = null;
		final int INDENT = 4;
		boolean result = false;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		try {
			MultipartEntityBuilder entity = MultipartEntityBuilder.create();
			ContentType type = ContentType.create(mimeType);
			entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addTextBody("params", json);
			entity.addBinaryBody(name, file, type, file.getName());
			httppost.setEntity(entity.build());
			HttpResponse httpResponse = httpclient.execute(httppost);
			HttpEntity httpEntity = httpResponse.getEntity();
			response = EntityUtils.toString(httpEntity);
			Log.e("URL", url);
			result = true;
		}
		catch(ClientProtocolException cpe) {
			exception = cpe.toString();
			message = cpe.getMessage();
		}
		catch(IOException ioe) {
			exception = ioe.toString();
			message = "You are getting weak internet connection. " +
					"Please find a reliable source to continue.";
		}
		if(!result) {
			try {
				JSONObject error = new JSONObject();
				JSONObject field = new JSONObject();
				field.put("message", message);
				field.put("exception", exception);
				error.put("error", field);
				response = error.toString(INDENT);
			}
			catch(JSONException je) {
				je.printStackTrace();
			}
		}
		return response;
	}

	public static String getImagePath(Context context, Uri uri) {
		String path = null;
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if(cursor != null) {
			cursor.moveToFirst();
			String documentID = cursor.getString(0);
			documentID = documentID.substring(documentID.lastIndexOf(":") + 1);
			cursor.close();
			cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					null, MediaStore.Images.Media._ID + " = ? ", new String[]{documentID}, null);
			if(cursor != null) {
				cursor.moveToFirst();
				path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				cursor.close();
			}
		}
		return path;
	}

	public static int getIntSharedPref(Context context, String sharedPref, String key) {
		SharedPreferences prefs = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
		int value = prefs.getInt(key, 0);
		return value;
	}

	public static String getKeyHash(Context context) {
		String keyHash = "";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			for(Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return keyHash;
	}

	public static int getLastRecordID(SQLiteAdapter db, String table) {
		int lastRecordID = 0;
		String query = "SELECT ID FROM " + table + " ORDER BY ID DESC LIMIT 1";
		lastRecordID = db.getInt(query);
		return lastRecordID;
	}

	public static String getMySQLPassword(String plainText) {
		String password = "";
		try {
			byte[] utf8 = plainText.getBytes("UTF-8");
			password = "*" + DigestUtils.shaHex(DigestUtils.sha(utf8)).toUpperCase();
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return password;
	}

	public static String getNameOfMonths(int month, boolean isAbbreviated, boolean isUpperCase) {
		String nameOfMonths = "";
		switch(month) {
			case 1:
				nameOfMonths = "January";
				break;
			case 2:
				nameOfMonths = "February";
				break;
			case 3:
				nameOfMonths = "March";
				break;
			case 4:
				nameOfMonths = "April";
				break;
			case 5:
				nameOfMonths = "May";
				break;
			case 6:
				nameOfMonths = "June";
				break;
			case 7:
				nameOfMonths = "July";
				break;
			case 8:
				nameOfMonths = "August";
				break;
			case 9:
				nameOfMonths = "September";
				break;
			case 10:
				nameOfMonths = "October";
				break;
			case 11:
				nameOfMonths = "November";
				break;
			case 12:
				nameOfMonths = "December";
				break;
		}
		if(isAbbreviated) {
			nameOfMonths = nameOfMonths.substring(0, 3);
		}
		if(isUpperCase) {
			String upperCase = "";
			for(int x = 0; x < nameOfMonths.length(); x++) {
				Character temp;
				temp = nameOfMonths.charAt(x);
				temp = Character.toUpperCase(temp);
				upperCase = upperCase + temp.toString();
			}
			nameOfMonths = upperCase;
		}
		return nameOfMonths;
	}

	public static String getStringSharedPref(Context context, String sharedPref, String key) {
		SharedPreferences prefs = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
		String value = prefs.getString(key, null);
		return value;
	}

	public static String getDay(String date) {
		long timestamp = dateTimeToMillis(date, "00:00:00");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return String.format(Locale.ENGLISH, "%tA", cal);
	}

	public static String getDate() {
		Calendar cal = Calendar.getInstance();
		return String.format(Locale.ENGLISH, "%tF", cal);
	}

	public static String getDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return String.format(Locale.ENGLISH, "%tF", cal);
	}

	public static String getTime() {
		Calendar cal = Calendar.getInstance();
		return String.format(Locale.ENGLISH, "%tT", cal);
	}

	public static String getTime(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return String.format(Locale.ENGLISH, "%tT", cal);
	}

	public static String getUTCTime() {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date(System.currentTimeMillis()));
	}

	public static String getVersionName(Context context) {
		String versionName = "2.0";
		final PackageManager packageManager = context.getPackageManager();
		if(packageManager != null) {
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
				versionName = packageInfo.versionName;
			}
			catch(NameNotFoundException e) {
				versionName = "2.0";
			}
		}
		return versionName;
	}

	public static String handleNullString(String text) {
		String result = "";
		if(text == null || text.equals("null")) {
			result = "";
		}
		else {
			result = text;
		}
		return result;
	}

	public static String handleQuotesUniCodeToSQLite(String text) {
		String result = "";
		if(text != null && !text.equals("null")) {
			result = text
					.replace("u0027", "''")
					.replace("u0022", "\"");
		}
		return result;
	}

	public static void showKeyboard(View v, Context context) {
		InputMethodManager manager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
		manager.showSoftInput(v, 0);
	}

	public static void showKeyboard(View v, Activity activity) {
		InputMethodManager manager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		manager.showSoftInput(v, 0);
	}

	public static void hideKeyboard(View v, Activity activity) {
		InputMethodManager manager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public static void hideKeyboard(View v, Context context) {
		InputMethodManager manager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public static boolean isGpsEnabled(Context context) {
		LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public static boolean hasInternet(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = cm.getActiveNetworkInfo();
		if(network != null) {
			return network.isAvailable();
		}
		return false;
	}

	public static boolean isMockEnabled(Context context) {
		boolean result = false;
		if(!Secure.getString(context.getContentResolver(), Secure.ALLOW_MOCK_LOCATION).equals("0")) {
			result = true;
		}
		return result;
	}

	public static boolean isNetEnabled(Context context) {
		LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	public static boolean isNumeric(String text) {
		if(text == null || text.isEmpty() || text.equals(".")) {
			return false;
		}
		int dotCount = 0;
		for(int i = 0; i < text.length(); i++) {
			if(!Character.isDigit(text.codePointAt((i)))) {
				String f = text.substring(i, i + 1);
				if(!f.equals(".")) {
					return false;
				}
				else {
					dotCount++;
					if(dotCount >= 2) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static boolean isServiceRunning(Context context, Class<?> c) {
		boolean result = false;
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if(c.getName().equals(service.service.getClassName())) {
				result = true;
			}
		}
		return result;
	}

	public static boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for(String string : subset) {
			if(!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isTimeEqual(int allowanceMin, String dateToCompare, String timeToCompare,
									  String baseDate, String baseTime) {
		boolean result = false;
		long millisToCompare = dateTimeToMillis(dateToCompare, timeToCompare);
		long millisBase = dateTimeToMillis(baseDate, baseTime);
		long millisAllowance = allowanceMin * 60000;
		long difference = millisToCompare > millisBase ? millisToCompare - millisBase : millisBase - millisToCompare;
		if(difference <= millisAllowance) {
			result = true;
		}
		return result;
	}

	public static boolean isValidEmail(String email) {
		return Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	public static boolean isValidURL(String url) {
		return URLUtil.isValidUrl(url) && Patterns.WEB_URL.matcher(url).matches();
	}

	public static void removeNotification(Context context, int notifID) {
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(notifID);
	}

	public static void setAlarm(Context context, Class<?> receiver, int durationMin, int requestCode) {
		if(durationMin > 0) {
			int timeRemainingSecs = durationMin * 60;
			Intent intent = new Intent(context, receiver);
			PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, 0);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (timeRemainingSecs * 1000), pi);
		}
	}

	public static void setBooleanSharedPref(Context context, String sharedPref, String key, boolean value) {
		SharedPreferences prefs = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(key, value).apply();
	}

	public static void setCircle(ImageView imageView) {
		Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
		Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
		Paint paint = new Paint();
		paint.setShader(shader);
		Canvas c = new Canvas(circleBitmap);
		c.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
		imageView.setImageBitmap(circleBitmap);
	}

	public static void setCrashHandler(final Context context, String folder, String password) {
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context, folder, password));
	}

	public static boolean setErrorMsg(Context context, String message, String folder, String password) {
		boolean result = false;
		String errorMsg = "Error: " + message;
		errorMsg = errorMsg.replace("\n", "\r\n");
		String fileName = CodePanUtils.getDate() + "_" + CodePanUtils.getTime() + ".txt";
		fileName = fileName.replace(":", "-");
		result = CodePanUtils.writeText(context, folder, fileName, errorMsg);
		if(result) {
			result = CodePanUtils.encryptFile(context, folder, fileName, password, ".txt");
		}
		return result;
	}

	public static boolean setErrorMsg(Context context, String message, String url, String jsonString,
									  String response, String folder, String password) {
		boolean result = false;
		String errorMsg = "Error: " + message + "\nURL: " + url + "\nParams: " +
				jsonString + "\nResponse: " + response;
		errorMsg = errorMsg.replace("\n", "\r\n");
		String fileName = CodePanUtils.getDate() + "_" + CodePanUtils.getTime() + ".txt";
		fileName = fileName.replace(":", "-");
		result = CodePanUtils.writeText(context, folder, fileName, errorMsg);
		if(result) {
			result = CodePanUtils.encryptFile(context, folder, fileName, password, ".txt");
		}
		return result;
	}

	public static void setGPS(Context context, boolean isEnabled) {
		try {
			Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
			intent.putExtra("enabled", isEnabled);
			context.sendBroadcast(intent);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void setIntSharedPref(Context context, String sharedPref, String key, int value) {
		SharedPreferences prefs = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
		prefs.edit().putInt(key, value).apply();
	}

	public static void setNotification(Context context, int ID, NotificationCompat.Builder builder) {
		@SuppressWarnings("static-access")
		NotificationManager notifManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		notifManager.notify(ID, builder.build());
	}

	public static void setNotification(Context context, String title, String message, int resource,
									   int ID, int requestCode, boolean isVibrate, Intent intent, String uri) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(resource);
		builder.setContentTitle(title);
		builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
		builder.setPriority(Notification.PRIORITY_HIGH);
		builder.setLights(Color.GREEN, 500, 500);
		builder.setOnlyAlertOnce(true);
		builder.setAutoCancel(true);
		if(isVibrate) {
			builder.setVibrate(new long[]{500, 500});
		}
		Uri url = Uri.parse(uri);
		builder.setSound(url);
		builder.setContentText(message);
		PendingIntent pi = PendingIntent.getActivity(context, requestCode,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pi);
		@SuppressWarnings("static-access")
		NotificationManager notifManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		notifManager.notify(ID, builder.build());
	}

	public static void setNotification(Context context, String title, String message, int resource,
									   int ID, boolean isVibrate, String uri) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(resource);
		builder.setContentTitle(title);
		builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
		builder.setPriority(Notification.PRIORITY_HIGH);
		builder.setLights(Color.GREEN, 500, 500);
		builder.setOnlyAlertOnce(true);
		builder.setAutoCancel(true);
		if(isVibrate) {
			builder.setVibrate(new long[]{500, 500});
		}
		Uri url = Uri.parse(uri);
		builder.setSound(url);
		builder.setContentText(message);
		@SuppressWarnings("static-access")
		NotificationManager notifManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		notifManager.notify(ID, builder.build());
	}

	public static void setStringSharedPref(Context context, String sharedPref, String key, String value) {
		SharedPreferences prefs = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
		prefs.edit().putString(key, value).apply();
	}

	public static String throwableToString(Throwable th) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		th.printStackTrace(pw);
		return sw.toString();
	}

	public static boolean writeText(Context context, String folderName, String fileName, String text) {
		boolean result = false;
		try {
			String path = context.getDir(folderName, Context.MODE_PRIVATE).getPath() + "/" + fileName;
			File file = new File(path);
			FileWriter writer = new FileWriter(file);
			writer.append(text);
			writer.flush();
			writer.close();
			result = true;
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean zipFile(Context context, String fileName, String folderName, String zipFileName) {
		String pathtozip = context.getDir(folderName, Context.MODE_PRIVATE).getPath() + "/" + fileName;
		String pathforzip = context.getDir(folderName, Context.MODE_PRIVATE).getPath() + "/" + zipFileName;
		int BUFFER = 80000;
		boolean result = false;
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(pathforzip);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			FileInputStream fi = new FileInputStream(pathtozip);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(pathtozip.substring(pathtozip.lastIndexOf("/") + 1));
			out.putNextEntry(entry);
			int count;
			while((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
			out.close();
			result = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean zipFolder(Context context, String folderToZip, String folderForZip, String zipFileName) {
		String pathtozip = context.getDir(folderToZip, Context.MODE_PRIVATE).getPath();
		String pathforzip = context.getDir(folderForZip, Context.MODE_PRIVATE).getPath() + "/" + zipFileName;
		boolean result = false;
		try {
			FileOutputStream fos = new FileOutputStream(pathforzip);
			ZipOutputStream zos = new ZipOutputStream(fos);
			File srcFile = new File(pathtozip);
			File[] files = srcFile.listFiles();
			for(int i = 0; i < files.length; i++) {
				byte[] buffer = new byte[1024];
				FileInputStream fis = new FileInputStream(files[i]);
				zos.putNextEntry(new ZipEntry(files[i].getName()));
				int length;
				while((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
				fis.close();
			}
			zos.close();
			result = true;
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean zipFolderExternal(Context context, String folderToZip, String folderName, String zipFileName) {
		String pathtozip = context.getDir(folderToZip, Context.MODE_PRIVATE).getPath();
		String pathforzip = Environment.getExternalStorageDirectory() + "/" + folderName + "/" + zipFileName;
		String folder = Environment.getExternalStorageDirectory() + "/" + folderName;
		File dir = new File(folder);
		if(!dir.exists()) {
			dir.mkdir();
		}
		boolean result = false;
		try {
			FileOutputStream fos = new FileOutputStream(pathforzip);
			ZipOutputStream zos = new ZipOutputStream(fos);
			File srcFile = new File(pathtozip);
			File[] files = srcFile.listFiles();
			for(int i = 0; i < files.length; i++) {
				byte[] buffer = new byte[1024];
				FileInputStream fis = new FileInputStream(files[i]);
				zos.putNextEntry(new ZipEntry(files[i].getName()));
				int length;
				while((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
				fis.close();
			}
			zos.close();
			result = true;
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void alertToast(FragmentActivity activity, String message, int duration) {
		int offsetY = activity.getResources().getDimensionPixelSize(R.dimen.one_hundred);
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_toast_layout, (ViewGroup) activity.findViewById(R.id.rlAlertToast));
		CodePanLabel text = (CodePanLabel) layout.findViewById(R.id.tvMessageAlertToast);
		text.setText(message);
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.BOTTOM, 0, offsetY);
		toast.setDuration(duration);
		toast.setView(layout);
		toast.show();
	}

	public static void alertToast(FragmentActivity activity, String message) {
		int offsetY = activity.getResources().getDimensionPixelSize(R.dimen.one_hundred);
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_toast_layout, (ViewGroup) activity.findViewById(R.id.rlAlertToast));
		CodePanLabel text = (CodePanLabel) layout.findViewById(R.id.tvMessageAlertToast);
		text.setText(message);
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.BOTTOM, 0, offsetY);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	public static void alertToast(FragmentActivity activity, SpannableStringBuilder ssb) {
		int offsetY = activity.getResources().getDimensionPixelSize(R.dimen.one_hundred);
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_toast_layout, (ViewGroup) activity.findViewById(R.id.rlAlertToast));
		CodePanLabel text = (CodePanLabel) layout.findViewById(R.id.tvMessageAlertToast);
		text.setText(ssb);
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.BOTTOM, 0, offsetY);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	public static void alertToast(FragmentActivity activity, SpannableStringBuilder ssb, int duration) {
		int offsetY = activity.getResources().getDimensionPixelSize(R.dimen.one_hundred);
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_toast_layout, (ViewGroup) activity.findViewById(R.id.rlAlertToast));
		CodePanLabel text = (CodePanLabel) layout.findViewById(R.id.tvMessageAlertToast);
		text.setText(ssb);
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.BOTTOM, 0, offsetY);
		toast.setDuration(duration);
		toast.setView(layout);
		toast.show();
	}

	public static void alertToast(FragmentActivity activity, int res) {
		int offsetY = activity.getResources().getDimensionPixelSize(R.dimen.one_hundred);
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_toast_layout, (ViewGroup) activity.findViewById(R.id.rlAlertToast));
		CodePanLabel text = (CodePanLabel) layout.findViewById(R.id.tvMessageAlertToast);
		text.setText(res);
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.BOTTOM, 0, offsetY);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	public static void alertToast(FragmentActivity activity, String message, int duration, ArrayList<SpannableMap> list, Typeface typeface) {
		int offsetY = activity.getResources().getDimensionPixelSize(R.dimen.one_hundred);
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_toast_layout, (ViewGroup) activity.findViewById(R.id.rlAlertToast));
		CodePanLabel text = (CodePanLabel) layout.findViewById(R.id.tvMessageAlertToast);
		SpannableStringBuilder ssb = new SpannableStringBuilder(message);
		for(SpannableMap obj : list) {
			ssb.setSpan(new CustomTypefaceSpan(typeface), obj.start, obj.end, android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}
		text.setText(ssb);
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.BOTTOM, 0, offsetY);
		toast.setDuration(duration);
		toast.setView(layout);
		toast.show();
	}

	public static void triggerHeartbeat(Context context) {
		context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
		context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
	}

	public static String convertBengaliNumerals(String text) {
		String eng = text.replace("০", "0")
				.replace("১", "1")
				.replace("২", "2")
				.replace("৩", "3")
				.replace("৪", "4")
				.replace("৫", "5")
				.replace("৬", "6")
				.replace("৭", "7")
				.replace("৮", "8")
				.replace("৯", "9");
		return eng;
	}

	public static boolean saveBitmap(Context context, String folder, String fileName, Bitmap bitmap) {
		boolean result = false;
		String path = context.getDir(folder, Context.MODE_PRIVATE).getPath() + "/" + fileName;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(path);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(out != null) {
					out.close();
					result = true;
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static boolean isThreadRunning(String name) {
		boolean result = false;
		Set<Thread> i = Thread.getAllStackTraces().keySet();
		for(Thread bg : i) {
			if(bg.getName().equals(name)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public static boolean isThreadRunning(String arg1, String arg2) {
		boolean result = false;
		Set<Thread> i = Thread.getAllStackTraces().keySet();
		for(Thread bg : i) {
			String name = bg.getName();
			if(name.equals(arg1) || name.equals(arg2)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		float scaleWidth = ((float) width) / w;
		float scaleHeight = ((float) height) / h;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, false);
	}

	public static Bitmap getBitmapThumbnails(Context context, String folderName, String fileName, int size) {
		String path = context.getDir(folderName, Context.MODE_PRIVATE).getPath() + "/" + fileName;
		File image = new File(path);
		BitmapFactory.Options bounds = new BitmapFactory.Options();
		bounds.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image.getPath(), bounds);
		if((bounds.outWidth == -1) || (bounds.outHeight == -1)) {
			return null;
		}
		int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = originalSize / size;
		return BitmapFactory.decodeFile(image.getPath(), opts);
	}

	public static int getSupportedNoOfCol(Context context) {
		final int numCol = 3;
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float widthDp = metrics.widthPixels / metrics.density;
		if(numCol != 0) {
			if(widthDp <= 360) {
				if(metrics.widthPixels % numCol == 0) {
					return numCol;
				}
				else {
					return numCol - 1;
				}
			}
			else {
				int x = numCol + 1;
				while(metrics.widthPixels % x != 0) {
					x++;
				}
				return x;
			}
		}
		else {
			return numCol;
		}
	}

	public static boolean isTablet(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float widthDp = metrics.widthPixels / metrics.density;
		return widthDp >= 600;
	}

	public static int getMaxWidth(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.widthPixels;
	}

	public static int getMaxHeight(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.heightPixels;
	}

	public static int pxToDp(Context context, int numCol) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return (numCol * (int) metrics.density);
	}

	public static int pxToDpOffset(Context context, int numCol) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return (int) (numCol * metrics.density);
	}

	public static int getWidth(View view) {
		int width = 0;
		if(view != null) {
			view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			width = view.getMeasuredWidth();
		}
		return width;
	}

	public static int getHeight(View view) {
		int height = 0;
		if(view != null) {
			view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			height = view.getMeasuredHeight();
		}
		return height;
	}

	public static void animateView(Context context, final View view, final int resID, final int filler) {
		Animation anim = AnimationUtils.loadAnimation(context, resID);
		anim.setFillAfter(true);
		view.startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if(view instanceof ImageView) {
					((ImageView) view).setImageResource(filler);
				}
				else {
					view.setBackgroundResource(filler);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
	}

	public static void fadeIn(final View view) {
		view.setVisibility(View.VISIBLE);
		Animation fadeIn = new AlphaAnimation(0.00f, 1.00f);
		fadeIn.setDuration(250);
		fadeIn.setInterpolator(new DecelerateInterpolator());
		fadeIn.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setEnabled(true);
			}
		});
		view.startAnimation(fadeIn);
	}

	public static void fadeIn(final View view, long duration) {
		view.setVisibility(View.VISIBLE);
		Animation fadeIn = new AlphaAnimation(0.00f, 1.00f);
		fadeIn.setDuration(duration);
		fadeIn.setInterpolator(new DecelerateInterpolator());
		fadeIn.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setEnabled(true);
			}
		});
		view.startAnimation(fadeIn);
	}

	public static void fadeOut(final View view) {
		view.setEnabled(false);
		Animation fadeOut = new AlphaAnimation(1.00f, 0.00f);
		fadeOut.setDuration(250);
		fadeOut.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
		view.startAnimation(fadeOut);
	}

	public static void fadeOut(final View view, long duration) {
		view.setEnabled(false);
		Animation fadeOut = new AlphaAnimation(1.00f, 0.00f);
		fadeOut.setDuration(duration);
		fadeOut.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
		view.startAnimation(fadeOut);
	}

	public static String handleUniCode(String text) {
		String result = "";
		if(text != null && !text.equals("null")) {
			result = text.replace("'", "''").
					replace("u0027", "\'").
					replace("u0022", "\"");
			result = unicodeToString(result);
		}
		return result;
	}

	public static String handleHTMLEntities(String text, boolean isHTML) {
		String result = "";
		if(text != null && !text.equals("null")) {
			if(isHTML) {
				result = text.replace("\n", "&NewLine;");
			}
			else {
				result = text.replace("&NewLine;", "\n")
						.replace("&Tab;", "    ");
			}
		}
		return result;
	}

	public static boolean isAppInstalled(Context context, String packageName) {
		try {
			PackageManager pm = context.getPackageManager();
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			return true;
		}
		catch(NameNotFoundException e) {
			return false;
		}
	}

	public static int getWindowHeight(Activity activity) {
		return activity.getWindowManager().getDefaultDisplay().getHeight();
	}

	public static int getWindowWidth(Activity activity) {
		return activity.getWindowManager().getDefaultDisplay().getWidth();
	}

	public static void setStatusBarColor(Activity activity, int resID) {
		if(resID != 0) {
			int color = activity.getResources().getColor(resID);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				Window window = activity.getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(color);
			}
		}
	}

	public static String[] getAppPermissions(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
					PackageManager.GET_PERMISSIONS);
			return info.requestedPermissions;
		}
		catch(NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void requestPermission(Activity activity, int requestCode) {
		String[] permissions = getDeniedPermission(activity);
		if(permissions != null) {
			ActivityCompat.requestPermissions(activity, permissions, requestCode);
		}
	}

	public static boolean isPermissionHidden(Activity activity) {
		String[] permissions = getDeniedPermission(activity);
		if(permissions != null) {
			for(String permission : permissions) {
				boolean result = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
				if(!result) {
					return true;
				}
			}
		}
		return false;
	}


	public static String[] getDeniedPermission(Context context) {
		String[] permissions = getAppPermissions(context);
		if(permissions != null) {
			List<String> deniedList = new ArrayList<>();
			for(String permission : permissions) {
				int result = ContextCompat.checkSelfPermission(context, permission);
				if(result == PackageManager.PERMISSION_DENIED) {
					deniedList.add(permission);
				}
			}
			return deniedList.toArray(new String[deniedList.size()]);
		}
		return null;
	}

	public static boolean isPermissionGranted(Context context) {
		String[] permissions = getAppPermissions(context);
		if(permissions != null) {
			for(String permission : permissions) {
				int result = ContextCompat.checkSelfPermission(context, permission);
				if(result == PackageManager.PERMISSION_DENIED) {
					return false;
				}
			}
			return true;
		}
		return true;
	}

	public static String getDisplayDate(String date) {
		Calendar cal = getCalendar(date);
		String dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH);
		return dayOfWeek + ", " + CodePanUtils.getCalendarDate(date, true, false);
	}

	public static String getDisplayYear(String date) {
		if(date != null) {
			return date.split("\\-")[0];
		}
		return null;
	}

	public static SpannableStringBuilder customizeText(ArrayList<SpannableMap> list, String text) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(text);
		for(SpannableMap span : list) {
			switch(span.type) {
				case SpannableMap.COLOR:
					ssb.setSpan(new ForegroundColorSpan(span.color), span.start, span.end, SPAN_INCLUSIVE_INCLUSIVE);
					break;
				case SpannableMap.FONT:
					ssb.setSpan(new CustomTypefaceSpan(span.typeface), span.start, span.end, SPAN_INCLUSIVE_INCLUSIVE);
					break;
			}
		}
		return ssb;
	}

	public static SpannableStringBuilder customizeText(Context context, String text, String font, char c) {
		if(text != null) {
			int index = 0;
			int start = 0;
			ArrayList<SpannableMap> map = new ArrayList<>();
			for(int i = 0; i < text.length(); i++) {
				if(text.charAt(i) == c) {
					if(index % 2 != 0) {
						int adj = map.size() * 2;
						int end = i - adj;
						start -= adj;
						map.add(new SpannableMap(context, font, start, end));
					}
					else {
						start = i;
					}
					index++;
				}
			}
			String replace = text.replace(String.valueOf(c), "");
			return customizeText(map, replace);
		}
		return null;
	}

	public static boolean withGooglePlayServices(final Activity activity) {
		boolean result = false;
		final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
		if(resultCode != ConnectionResult.SUCCESS) {
			if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 1);
				if(dialog != null) {
					dialog.show();
					dialog.setCancelable(true);
					dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							if(ConnectionResult.SERVICE_INVALID == resultCode) {
								activity.finish();
							}
						}
					});
					dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							activity.finish();
						}
					});
				}
			}
		}
		else {
			result = true;
		}
		return result;
	}

	public static String validateURL(String url) {
		String https = "https://";
		String http = "http://";
		if(url != null) {
			if(!url.contains(https) && !url.contains(http)) {
				return https + url;
			}
		}
		return url;
	}

	public static boolean clearImageUrl(Context context, String url) {
		boolean result = false;
		ImageLoader imageLoader = ImageLoader.getInstance();
		if(!imageLoader.isInited()) {
			imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		}
		File image = imageLoader.getDiskCache().get(url);
		if(image.exists()) {
			result = image.delete();
		}
		else {
			result = true;
		}
		imageLoader.clearDiskCache();
		imageLoader.clearMemoryCache();
		return result;
	}

	public static void displayImage(ImageView view, String uri) {
		if(view != null) {
			ImageLoader imageLoader = ImageLoader.getInstance();
			if(!imageLoader.isInited()) {
				imageLoader.init(ImageLoaderConfiguration.createDefault(view.getContext()));
			}
			DisplayImageOptions options = new DisplayImageOptions.Builder()
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.build();
			imageLoader.displayImage(uri, view, options);
		}
	}

	public static void displayImage(ImageView view, String uri, ImageLoadingListener listener) {
		if(view != null) {
			ImageLoader imageLoader = ImageLoader.getInstance();
			if(!imageLoader.isInited()) {
				imageLoader.init(ImageLoaderConfiguration.createDefault(view.getContext()));
			}
			DisplayImageOptions options = new DisplayImageOptions.Builder()
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.build();
			imageLoader.displayImage(uri, view, options, listener);
		}
	}

	public static void displayImage(ImageView view, String uri, int placeholder) {
		if(view != null) {
			ImageLoader imageLoader = ImageLoader.getInstance();
			if(!imageLoader.isInited()) {
				imageLoader.init(ImageLoaderConfiguration.createDefault(view.getContext()));
			}
			DisplayImageOptions options = new DisplayImageOptions.Builder()
					.showImageOnLoading(placeholder)
					.showImageForEmptyUri(placeholder)
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.build();
			imageLoader.displayImage(uri, view, options);
		}
	}

	public static void displayImage(ImageView view, String uri, int placeholder,
									ImageLoadingListener listener) {
		if(view != null) {
			ImageLoader imageLoader = ImageLoader.getInstance();
			if(!imageLoader.isInited()) {
				imageLoader.init(ImageLoaderConfiguration.createDefault(view.getContext()));
			}
			DisplayImageOptions options = new DisplayImageOptions.Builder()
					.showImageOnLoading(placeholder)
					.showImageForEmptyUri(placeholder)
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.build();
			imageLoader.displayImage(uri, view, options, listener);
		}
	}

	public static GpsObj getGps(Context context, Location location,
								long lastLocationUpdate, long interval, float requiredAccuracy) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		long millis = 0;
		double latitude = 0d;
		double longitude = 0d;
		float accuracy = 0f;
		float speed = 0f;
		boolean isEnabled = false;
		boolean withHistory = false;
		boolean isValid = false;
		String gpsTime = "00:00:00";
		String gpsDate = "0000-00-00";
		if(isGpsEnabled(context)) {
			if(location != null) {
				millis = location.getTime();
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				accuracy = location.getAccuracy();
				speed = location.getSpeed();
				long timeElapsed = SystemClock.elapsedRealtime() - lastLocationUpdate;
				long allowance = speed >= 20 ? 0 : 15000;
				if(timeElapsed <= interval + allowance && accuracy <= requiredAccuracy) {
					if(longitude != 0 && latitude != 0) {
						isValid = true;
					}
				}
				java.util.Date gps = new java.util.Date(millis);
				gpsTime = timeFormat.format(gps);
				gpsDate = dateFormat.format(gps);
				withHistory = true;
			}
			isEnabled = true;
		}
		GpsObj gps = new GpsObj();
		gps.longitude = longitude;
		gps.latitude = latitude;
		gps.accuracy = accuracy;
		gps.millis = millis;
		gps.time = gpsTime;
		gps.date = gpsDate;
		gps.isEnabled = isEnabled;
		gps.withHistory = withHistory;
		gps.speed = speed;
		gps.isValid = isValid;
		return gps;
	}

	public static boolean isOnBackStack(FragmentActivity activity, String tag) {
		FragmentManager manager = activity.getSupportFragmentManager();
		Fragment fragment = manager.findFragmentByTag(tag);
		return fragment != null && fragment.isVisible();
	}

	public static String formatDate(String date) {
		if(date != null && !date.isEmpty()) {
			String array[] = date.split("-");
			int m = Integer.valueOf(array[1]);
			int d = Integer.valueOf(array[2]);
			String month = String.format(Locale.ENGLISH, "%02d", m);
			String day = String.format(Locale.ENGLISH, "%02d", d);
			return array[0] + "-" + month + "-" + day;
		}
		else {
			return null;
		}
	}

	public static String formatTime(String time) {
		if(time != null && !time.isEmpty()) {
			String array[] = time.split(":");
			int h = Integer.valueOf(array[0]);
			int m = Integer.valueOf(array[1]);
			int s = Integer.valueOf(array[2]);
			String hour = String.format(Locale.ENGLISH, "%02d", h);
			String min = String.format(Locale.ENGLISH, "%02d", m);
			String sec = String.format(Locale.ENGLISH, "%02d", s);
			return hour + ":" + min + ":" + sec;
		}
		else {
			return null;
		}
	}

	public static String formatDateTime(String strToFormat, String currentFormat, String newFormat) {
		String currentDate = strToFormat;
		SimpleDateFormat format = new SimpleDateFormat(currentFormat, Locale.ENGLISH);
		java.util.Date date = new java.util.Date();
		try {
			date = format.parse(currentDate);
			format = new SimpleDateFormat(newFormat, Locale.ENGLISH);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		String newDate = format.format(date);
		return newDate;
	}

	public static void logHttpRequest(String params, String response) {
		if(params != null) Log.e("PARAMS", params);
		if(response != null) Log.e("RESPONSE", response);
	}

	public static String capitalizeWord(String text) {
		if(text != null && !text.isEmpty()) {
			String[] words = text.trim().split(" ");
			StringBuilder ret = new StringBuilder();
			for(int i = 0; i < words.length; i++) {
				if(words[i].trim().length() > 0) {
					ret.append(Character.toUpperCase(words[i].trim().charAt(0)));
					ret.append(words[i].trim().substring(1));
					if(i < words.length - 1) {
						ret.append(' ');
					}
				}
			}
			return ret.toString();
		}
		return text;
	}

	public static boolean isViewVisible(ScrollView sv, View v) {
		Rect rect = new Rect();
		sv.getHitRect(rect);
		return v.getLocalVisibleRect(rect);
	}

	public static String getRawString(Context context, int res) {
		String result = null;
		Resources resources = context.getResources();
		try {
			InputStream is = resources.openRawResource(res);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while((length = is.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}
			result = bos.toString("UTF-8");
			is.close();
			bos.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String handleSQLite(String text) {
		if(text != null) {
			return text.replace("''", "'");
		}
		return null;
	}

	public static void sendSMS(String address, String message) {
		if(message != null && !message.isEmpty() &&
				address != null && !address.isEmpty()) {
			SmsManager manager = SmsManager.getDefault();
			manager.sendTextMessage(address, null, message, null, null);
		}
	}

	public static void requiredField(CodePanLabel label, String text) {
		if(text != null) {
			int length = text.length();
			String name = text + "*";
			ArrayList<SpannableMap> list = new ArrayList<>();
			list.add(new SpannableMap(length, length + 1, Color.RED));
			SpannableStringBuilder ssb = CodePanUtils.customizeText(list, name);
			label.setText(ssb);
		}
	}

	public static void requiredField(CodePanLabel label) {
		if(label != null) {
			String text = label.getText().toString();
			int length = text.length();
			String name = text + "*";
			ArrayList<SpannableMap> list = new ArrayList<>();
			list.add(new SpannableMap(length, length + 1, Color.RED));
			SpannableStringBuilder ssb = CodePanUtils.customizeText(list, name);
			label.setText(ssb);
		}
	}
}
