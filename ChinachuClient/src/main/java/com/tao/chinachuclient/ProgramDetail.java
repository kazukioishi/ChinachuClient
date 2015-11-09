package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import Chinachu4j.Chinachu4j;
import Chinachu4j.ChinachuResponse;
import Chinachu4j.Program;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.tao.chinachuclient.CastSupport.CastApplication;

public class ProgramDetail extends AppCompatActivity {

	private String fullTitle, programId;
	private int type;
	private ApplicationClass appClass;
	private String capture;
	private int seconds;
	private int randomSecond;
	private VideoCastManager mCastManager;
	private Program prog;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_program_detail);

		appClass = (ApplicationClass)getApplicationContext();

		Program tmp = (Program)appClass.getTmp();
		prog = tmp;

		android.support.v7.app.ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(false);
		actionbar.setTitle(tmp.getTitle());

		programId = tmp.getId();
		fullTitle = tmp.getFullTitle();
		String detail = tmp.getDetail();
		long start = tmp.getStart();
		long end = tmp.getEnd();
		seconds = tmp.getSeconds();
		String category = tmp.getCategory();
		String[] flags = tmp.getFlags();
		String channelType = tmp.getChannel().getType();
		String channelName = tmp.getChannel().getName();
		type = getIntent().getIntExtra("type", -1);

		final ImageView image = (ImageView)findViewById(R.id.programs_detail_image);
		if(type == 3 || type == 4) {
			image.setVisibility(View.VISIBLE);
			//Prepare for chromecast
			// Google Play Serviceのチェック
			BaseCastManager.checkGooglePlayServices(this);
			mCastManager = CastApplication.getCastManager(this);

		}else
			image.setVisibility(View.GONE);

		TextView detailView = (TextView)findViewById(R.id.program_detail_detail);
		String detailText = "<p><strong>フルタイトル</strong><br />" + fullTitle + "<br /></p><p><strong>詳細</strong><br />" + detail + "</p>";
		detailView.setText(Html.fromHtml(detailText));

		TextView otherView = (TextView)findViewById(R.id.program_detail_other);

		String startStr = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPANESE).format(new Date(start));
		String endStr = new SimpleDateFormat("HH:mm", Locale.JAPANESE).format(new Date(end));
		String minute = seconds / 60 + "分間";
		String flag = "";
		for(String s : flags)
			flag += s + ", ";
		if(flag.length() == 0)
			flag = "なし";
		else
			flag = flag.substring(0, flag.length() - 2);
		String otherText = "<p>" + startStr + " 〜 " + endStr + " (" + minute + ")<br /><br />" + category + " / " + channelType
				+ ": " + channelName + "<br /><br />フラグ：" + flag + "<br /><br />id：" + programId + "</p>";
		otherView.setText(Html.fromHtml(otherText));

		if(type == 3 || type == 4) {
			AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>(){
				@Override
				protected String doInBackground(Void... params){
					try{
						if(type == 3)
							return appClass.getChinachu().getRecordingImage(programId, "1280x720");
						if(type == 4) {
							randomSecond = new Random().nextInt(seconds) + 1;
							return appClass.getChinachu().getRecordedImage(programId, randomSecond, "1280x720");
						}
						return null;
					}catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
						return null;
					}
				}

				@Override
				protected void onPostExecute(String result){
					if(result == null) {
						Toast.makeText(ProgramDetail.this, "画像取得エラー", Toast.LENGTH_SHORT).show();
						return;
					}
					if(result.startsWith("data:image/jpeg;base64,"))
						result = result.substring(23);
					byte[] decodedString = Base64.decode(result, Base64.DEFAULT);
					Bitmap img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
					image.setImageBitmap(img);
					capture = result;
				}
			};
			task.execute();
		}
	}

	@SuppressLint("InflateParams")
	public void imageClick(View v){
		View view = LayoutInflater.from(this).inflate(R.layout.capture_dialog, null);
		final EditText cap_pos = (EditText)view.findViewById(R.id.cap_pos);
		final EditText cap_size = (EditText)view.findViewById(R.id.cap_size);
		final SeekBar cap_seek = (SeekBar)view.findViewById(R.id.cap_seek);

		if(type == 3){
			cap_pos.setVisibility(View.GONE);
			cap_seek.setVisibility(View.GONE);
		}else if(type == 4){
			cap_pos.setText(String.valueOf(randomSecond));
			final float textSize = cap_pos.getTextSize();
			cap_pos.setWidth((int)((String.valueOf(seconds).length() + 1) * textSize));
			cap_seek.setMax(seconds - 10);
			cap_seek.setProgress(randomSecond);
			cap_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

				@Override
				public void onStopTrackingTouch(SeekBar seekBar){
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar){
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
					cap_pos.setText(String.valueOf(progress));
				}
			});
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setView(view)
		.setNegativeButton("キャンセル", null)
		.setPositiveButton("OK", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which){
				AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>(){
					private ProgressDialog progDailog;

					@Override
					protected void onPreExecute(){
						progDailog = new ProgressDialog(ProgramDetail.this);
						progDailog.setMessage("Loading...");
						progDailog.setIndeterminate(false);
						progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progDailog.setCancelable(true);
						progDailog.show();
					}

					@Override
					protected String doInBackground(Void... params){
						try{
							if(type == 3){
								return appClass.getChinachu().getRecordingImage(programId, cap_size.getText().toString());
							}
							if(type == 4){
								return appClass.getChinachu().getRecordedImage(programId, Integer.parseInt(cap_pos.getText().toString()),
										cap_size.getText().toString());
							}
							return null;
						}catch(KeyManagementException | NumberFormatException | NoSuchAlgorithmException | IOException e){
							return null;
						}
					}

					@Override
					protected void onPostExecute(String result){
						progDailog.dismiss();
						if(result == null){
							Toast.makeText(ProgramDetail.this, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show();
							return;
						}
						if(result.startsWith("data:image/jpeg;base64,"))
							result = result.substring(23);
						Intent i = new Intent(ProgramDetail.this, Show_Image.class);
						i.putExtra("base64", result);
						startActivity(i);
					}
				};
				task.execute();
			}
		});
		builder.setNeutralButton("このまま拡大", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which){
				Intent i = new Intent(ProgramDetail.this, Show_Image.class);
				i.putExtra("base64", capture);
				startActivity(i);
			}
		});
		builder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.program_detail, menu);
		if(mCastManager!=null) mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
		if(type == 0 || type == 5)
			menu.add("予約");
		if(type == 2)
			menu.add("予約削除");
		if(type == 3 || type == 4) {
			if(appClass.getStreaming())
				menu.add("ストリーミング再生");
			if(appClass.getEncStreaming()) {
				menu.add("ストリーミング再生(エンコ有)");
			}
		}
		if(type == 4)
			menu.add("録画ファイル削除");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		CharSequence test = item.getTitle();
		//TODO:この比較方法はよくない(と思うけど適当にエラーだけは潰しておきます)
		if(item.getTitle() == null){
			finish();
			return super.onOptionsItemSelected(item);
		}
		if(item.getTitle().equals("ストリーミング再生")) {
			if(type == 3 || type == 4) {
				Uri uri = Uri.parse(appClass.getChinachu().getNonEncRecordingMovie(programId));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		}else if(item.getTitle().equals("ストリーミング再生(エンコ有)")) {
			String[] params = new String[7];
			SharedPreferences enc = getSharedPreferences("encodeConfig", MODE_PRIVATE);
			String t = enc.getString("type", null);
			params[0] = enc.getString("containerFormat", null);
			params[1] = enc.getString("videoCodec", null);
			params[2] = enc.getString("audioCodec", null);
			params[3] = enc.getString("videoBitrate", null);
			params[4] = enc.getString("audioBitrate", null);
			params[5] = enc.getString("videoSize", null);
			params[6] = enc.getString("frame", null);
			//録画中のもの
			if(type == 3) {
				Uri uri = Uri.parse(appClass.getChinachu().getEncRecordingMovie(programId, t, params));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			//録画完了したもの
			if(type == 4) {
				Uri uri = Uri.parse(appClass.getChinachu().getEncRecordedMovie(programId, t, params));
				if(mCastManager.isConnected()){
					String encpath = uri.toString();
					MediaInfo mediaInfo = createMediaInfo(encpath,prog.getFullTitle());
					loadTargetMedia(mediaInfo);
				}else{
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
			}
		}else if(type == 0 || type == 2 || type == 4 || type == 5) {
			Confirm();
		}

		if(item.getItemId() == android.R.id.home)
			finish();
		return super.onOptionsItemSelected(item);
	}

	public void Confirm(){
		final Chinachu4j chinachu = appClass.getChinachu();

		AlertDialog.Builder before = new Builder(this);
		switch(type){
		case 0:
		case 5:
			before.setTitle("予約しますか？");
			break;
		case 2:
			before.setTitle("予約を削除しますか？");
			break;
		case 4:
			before.setTitle("録画ファイルを削除しますか？");
			break;
		}
		before.setMessage(fullTitle).setNegativeButton("キャンセル", null).setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				AsyncTask<Void, Void, ChinachuResponse> task = new AsyncTask<Void, Void, ChinachuResponse>(){
					private ProgressDialog progDailog;

					@Override
					protected void onPreExecute(){
						progDailog = new ProgressDialog(ProgramDetail.this);
						progDailog.setMessage("Sending...");
						progDailog.setIndeterminate(false);
						progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progDailog.setCancelable(true);
						progDailog.show();
					}

					@Override
					protected ChinachuResponse doInBackground(Void... params){
						try{
							switch(type){
							case 0:
							case 5:
								return chinachu.putReserve(programId);
							case 2:
								return chinachu.delReserve(programId);
							case 4:
								return chinachu.delRecordedFile(programId);
							}
							return null;
						}catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
							return null;
						}
					}

					@Override
					protected void onPostExecute(ChinachuResponse result){
						progDailog.dismiss();
						if(result == null) {
							Toast.makeText(ProgramDetail.this, "通信エラー", Toast.LENGTH_SHORT).show();
							return;
						}
						if(!result.getResult()) {
							Toast.makeText(ProgramDetail.this, result.getMessage(), Toast.LENGTH_LONG).show();
							return;
						}

						AlertDialog.Builder after = new Builder(ProgramDetail.this);
						switch(type){
						case 0:
						case 5:
							after.setTitle("予約完了");
							after.setMessage(fullTitle);
							break;
						case 2:
							after.setTitle("予約の削除完了");
							after.setMessage(fullTitle);
							break;
						case 4:
							after.setTitle("録画ファイルの削除完了");
							after.setMessage(fullTitle + "\n\n録画済みリストへの反映にはクリーンアップが必要です");
							break;
						}
						after.create().show();
					}
				};
				task.execute();
			}
		});
		before.create().show();
	}
	//Chromecast関連
	@Override
	protected void onPause() {
		super.onPause();
		if (mCastManager != null) {
			mCastManager.decrementUiCounter();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCastManager != null) {
			//mCastManager.clearContext(this);
			mCastManager.clearMediaSession();
		}
	}
	/**
	 * Chrome Castに再生を支持するためのActivityを起動する。
	 *
	 * @param media 再生対象メディア
	 */
	private void loadTargetMedia(final MediaInfo media) {
		mCastManager.startVideoCastControllerActivity(this, media, 0, true);
	}

	/**
	 * メディア情報を生成する。
	 *
	 * @return メディア情報
	 */
	private MediaInfo createMediaInfo(String mediaURL,String Title) {
		// メタ情報
		MediaMetadata videoMetaData = new MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC);
		// タイトル
		videoMetaData.putString(MediaMetadata.KEY_TITLE, Title);
		MediaInfo mediaInfo = new MediaInfo.Builder(mediaURL)
				.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
				.setContentType("video/webm")
				.setMetadata(videoMetaData)
				.build();
		return mediaInfo;
	}

}