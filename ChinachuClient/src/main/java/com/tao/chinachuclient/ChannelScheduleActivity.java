package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import org.json.JSONException;

import Chinachu4j.Chinachu4j;
import Chinachu4j.Program;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;

public class ChannelScheduleActivity extends AppCompatActivity implements android.support.v7.app.ActionBar.OnNavigationListener{

	private ArrayAdapter<String> spinnerAdapter;

	private String[] channelIdList;

	private ListView programList;
	private ProgramListAdapter programListAdapter;

	private Chinachu4j chinachu;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		programList = new ListView(this);
		setContentView(programList);

		android.support.v7.app.ActionBar actionbar = getSupportActionBar();
		actionbar.setNavigationMode(android.support.v7.app.ActionBar.NAVIGATION_MODE_LIST);
		actionbar.setTitle("番組表");
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(false);

		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
		// チャンネルリストの取得
		SharedPreferences channels = getSharedPreferences("channels", MODE_PRIVATE);
		channelIdList = channels.getString("channelIds", null).split(",", 0);
		spinnerAdapter.addAll(channels.getString("channelNames", null).split(",", 0));

		actionbar.setListNavigationCallbacks(spinnerAdapter, this);

		programListAdapter = new ProgramListAdapter(this);
		programList.setAdapter(programListAdapter);
		programList.setOnItemClickListener(new ProgramListClickListener(this, 0));

		chinachu = ((ApplicationClass)getApplicationContext()).getChinachu();

	}

	// ActionBarのSpinnerで選択された時呼ばれる
	@Override
	public boolean onNavigationItemSelected(final int itemPosition, long itemId){
		AsyncTask<Void, Void, Program[]> task = new AsyncTask<Void, Void, Program[]>(){
			private ProgressDialog progDailog;

			@Override
			protected void onPreExecute(){
				progDailog = new ProgressDialog(ChannelScheduleActivity.this);
				progDailog.setMessage("Loading...");
				progDailog.setIndeterminate(false);
				progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDailog.setCancelable(true);
				progDailog.show();
			}

			@Override
			protected Program[] doInBackground(Void... params){
				try{
					return chinachu.getChannelSchedule(channelIdList[itemPosition]);
				}catch(KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Program[] result){
				progDailog.dismiss();
				programListAdapter.clear();
				if(result == null) {
					Toast.makeText(ChannelScheduleActivity.this, "番組取得エラー", Toast.LENGTH_SHORT).show();
					return;
				}
				programListAdapter.addAll(result);
			}
		};
		task.execute();
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.search, menu);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_view));
		searchView.setQueryHint("全チャンネルから番組検索");
		searchView.setOnQueryTextListener(new OnQueryTextListener(){

			@Override
			public boolean onQueryTextSubmit(String query){
				Intent i = new Intent(ChannelScheduleActivity.this, ProgramActivity.class);
				i.putExtra("type", 5);
				i.putExtra("query", query);
				startActivity(i);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText){
				return false;
			}
		});
		return true;
	}
}