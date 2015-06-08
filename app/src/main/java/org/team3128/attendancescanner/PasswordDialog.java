package org.team3128.attendancescanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Class which contains code for showing a password dialog.
 */
public class PasswordDialog
{
	//SHA-256 sum of the mentor password
	//bet you thought that you could find out what it was by looking here on GitHub, didn't you!
	private final static byte[] mentorPasswordSHA256 = {(byte) 0xae, (byte) 0xe6, (byte) 0xbc, (byte) 0xd3, (byte) 0x4a, (byte) 0xb5,
			(byte) 0xaf, (byte) 0xbf, (byte) 0xc1, (byte) 0x01, (byte) 0xf8, (byte) 0x1b, (byte) 0xed, (byte) 0x4b,
			(byte) 0x06, (byte) 0xeb, (byte) 0xbe, (byte) 0xd3, (byte) 0x3c, (byte) 0x2b, (byte) 0xc0, (byte) 0x52,
			(byte) 0xbb, (byte) 0xd8, (byte) 0x6c, (byte) 0x44, (byte) 0x96, (byte) 0xb5, (byte) 0x8c,
			(byte) 0x41, (byte) 0x31, (byte) 0x82};
	/**
	 * Show a dialog requesting a password.
	 * @param context context to use to create the dialog
	 * @param inflater
	 * @param executeIfFailed lambda to execute if the user does not enter the password
	 */
	public static void show(final Context context, LayoutInflater inflater, final Runnable executeIfFailed)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// layout and inflater
		View content = inflater.inflate(R.layout.dialog_password, null);
		builder.setView(content);

		builder.setTitle(R.string.password_dialog_title);

		final EditText passwordText = (EditText) content.findViewById(R.id.passwordEditText);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				byte[] entered = null;
				try
				{
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					entered = digest.digest(passwordText.getText().toString().getBytes("UTF-8"));

				}
				catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
				{
					//this shouldn't happen unless the hardcoded strings are wrong
					e.printStackTrace();
				}

				if(!Arrays.equals(entered, mentorPasswordSHA256))
				{
					Toast.makeText(context, R.string.password_incorrect, Toast.LENGTH_SHORT).show();
					executeIfFailed.run();
				}
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				executeIfFailed.run();
			}
		});

		//builder.setOnDismissListener(new DialogInterface.OnDismissListener()
//		{
//
//			@Override
//			public void onDismiss(DialogInterface dialog)
//			{
//				executeIfFailed.run();
//			}
//		});

		builder.show();
	}
}
