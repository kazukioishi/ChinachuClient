package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import android.support.v7.app.AppCompatActivity;
import org.json.JSONException;

import Chinachu4j.Rule;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class RuleActivity extends AppCompatActivity implements OnRefreshListener, OnItemClickListener{

	private ListView list;
	private SwipeRefreshLayout swipeRefresh;
	private RuleListAdapter adapter;
	private ApplicationClass appClass;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_program);

		appClass = (ApplicationClass)getApplicationContext();

		list = (ListView)findViewById(R.id.programList);
		adapter = new RuleListAdapter(this);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
		swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"));
		swipeRefresh.setOnRefreshListener(this);

		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("ルール");
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);

		AsyncTask<Void, Void, Rule[]> task = new AsyncTask<Void, Void, Rule[]>(){
			private ProgressDialog progDailog;

			@Override
			protected void onPreExecute(){
				progDailog = new ProgressDialog(RuleActivity.this);
				progDailog.setMessage("Loading...");
				progDailog.setIndeterminate(false);
				progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDailog.setCancelable(true);
				progDailog.show();
			}

			@Override
			protected Rule[] doInBackground(Void... params){
				try{
					return appClass.getChinachu().getRules();
				}catch(KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Rule[] result){
				progDailog.dismiss();
				if(result == null) {
					Toast.makeText(RuleActivity.this, "ルール取得エラー", Toast.LENGTH_SHORT).show();
					return;
				}
				adapter.addAll(result);
			}
		};
		task.execute();
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
	public void onRefresh(){
		adapter.clear();
		AsyncTask<Void, Void, Rule[]> task = new AsyncTask<Void, Void, Rule[]>(){
			@Override
			protected Rule[] doInBackground(Void... params){
				try{
					return appClass.getChinachu().getRules();
				}catch(KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Rule[] result){
				swipeRefresh.setRefreshing(false);
				if(result == null) {
					Toast.makeText(RuleActivity.this, "ルール取得エラー", Toast.LENGTH_SHORT).show();
					return;
				}
				adapter.addAll(result);
			}
		};
		task.execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		Rule tmp = (Rule)parent.getItemAtPosition(position);
		appClass.setTmp(tmp);
		Intent i = new Intent(this, RuleDetail.class);
		i.putExtra("position", String.valueOf(position));
		startActivity(i);
	}
}
