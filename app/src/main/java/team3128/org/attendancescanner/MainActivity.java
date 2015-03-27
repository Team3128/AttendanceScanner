package team3128.org.attendancescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class MainActivity extends ActionBarActivity
{
	public TextView consoleTextView;

	public Button scanButton;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		consoleTextView = (TextView) findViewById(R.id.consoleTextView);
		scanButton = (Button) findViewById(R.id.scanButton);
		scanButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				invokeScan();
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	private void invokeScan()
	{
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.addExtra("SCAN_WIDTH", 800);
		integrator.addExtra("SCAN_HEIGHT", 300);
		integrator.addExtra("RESULT_DISPLAY_DURATION_MS", 0);
		integrator.addExtra("PROMPT_MESSAGE", "Scan Student ID");
		integrator.initiateScan(IntentIntegrator.ONE_D_CODE_TYPES);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null)
		{
			String contents = result.getContents();
			if(contents != null)
			{
				consoleTextView.append(contents + "\n");
			}
			consoleTextView.append("Scan Error");
		}
		else
		{
			consoleTextView.append("Scan Cancelled");
		}
	}

	@Override
	public void onPause()
	{
		super.onResume();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
