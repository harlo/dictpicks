package com.msteg.dictpicks;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DictPicks extends Activity implements View.OnClickListener {
	private Button a_button;
	private TextView a_output;
	
	private class AOStruct {
		private String word;
		private int freq;
	}
	
	private final static String LOG = "***************** Dict Picks *****************";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dict_picks);
		
		initLayout();
	}
	
	private void initLayout() {
		a_button = (Button) findViewById(R.id.analyze_button);
		a_button.setOnClickListener(this);
		
		a_output = (TextView) findViewById(R.id.analyze_output);
	}
	
	private void exportDictionary(ArrayList<AOStruct> aos) {
		ArrayList<String> ao_csv = new ArrayList<String>();
		ao_csv.add("word,frequency");
		
		for(AOStruct ao : aos) {
			ao_csv.add(ao.word + "," + ao.freq);
		}
		
		Intent send_csv = new Intent();
		send_csv.setAction(Intent.ACTION_SEND);
		send_csv.putExtra(Intent.EXTRA_EMAIL, new String[] {"harlo.holmes@gmail.com"});
		send_csv.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.hello_from_dictpicks));
		send_csv.putExtra(Intent.EXTRA_TEXT, TextUtils.join("\n", ao_csv));
		send_csv.setType("text/plain");
		
		startActivity(Intent.createChooser(send_csv, getString(R.string.send_email_with)));
	}
	
	private void analyzeDictionary() {
		Log.d(LOG, getString(R.string.analyzing_custom_dictionary));
		
		a_output.setText("");
		final ArrayList<AOStruct> ao_list = new ArrayList<AOStruct>();
		
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(UserDictionary.Words.CONTENT_URI, null, null, null, null);
		
		int word_idx = c.getColumnIndex(UserDictionary.Words.WORD);
		int freq_idx = c.getColumnIndex(UserDictionary.Words.FREQUENCY);
		
		while(c.moveToNext()) {
			AOStruct ao = new AOStruct();
			ao.word = c.getString(word_idx);
			ao.freq = c.getInt(freq_idx);
			
			ao_list.add(ao);
		}
		
		if(ao_list.isEmpty()) {
			a_output.setText(getString(R.string.no_custom_words_found));
			return;
		}
		
		ArrayList<String> ao_text = new ArrayList<String>();
		for(AOStruct ao : ao_list) {
			ao_text.add(String.format("%s (frequency: %d)", ao.word, ao.freq));
		}
		
		a_output.setText(TextUtils.join("\n", ao_text));
		
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle(R.string.action_export);
		ad.setMessage(R.string.send_email_now);
		ad.setPositiveButton(getString(R.string.yes), new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				exportDictionary(ao_list);
			}
			
		});
		ad.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dict_picks, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_export) {
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if(v == a_button) {
			analyzeDictionary();
		}
		
	}
}
