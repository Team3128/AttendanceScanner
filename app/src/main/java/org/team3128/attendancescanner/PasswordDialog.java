package org.team3128.attendancescanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
	/**
	 * Used to remember the password entered so it can be saved in onSaveInstanceState
	 */
	static byte[] lastPasswordHash;

	/**
	 * Used to tell the onDismissListener whether the user entered the right password or not.
	 *
	 * This really should be an instance variable, but I'm using a dialog builder, so I can't do that.
	 */
	static boolean gaveCorrectPassword;

	/**
	 * Show a dialog requesting a password.
	 * @param context context to use to create the dialog
	 * @param inflater the LayoutInflater to use for the dialog
	 * @param executeIfFailed lambda to execute if the user does not enter the password
	 */
	public static void show(final Context context, Bundle savedInstanceState, LayoutInflater inflater, final byte[] passwordSHA256, final Runnable executeIfFailed)
	{
		//check if user previously entered
		if(savedInstanceState != null)
		{
			String lastPassword = savedInstanceState.getString("lastPassword");

			if(lastPassword != null)
			{
				byte[] lastPasswordBytes = Base64.decode(lastPassword, Base64.NO_WRAP);

				if(Arrays.equals(passwordSHA256, lastPasswordBytes))
				{
					return;
				}

				Log.e("PasswordDialog", "...Huh?  The password stored in savedInstanceState was incorrect!  How did this happen?");
			}
		}

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

				gaveCorrectPassword = checkPassword(passwordSHA256, passwordText.getText().toString());

				if(!gaveCorrectPassword)
				{
					Toast.makeText(context, R.string.password_incorrect, Toast.LENGTH_SHORT).show();

				}
			}
		});

		builder.setNegativeButton(android.R.string.cancel, null);

		builder.setOnDismissListener(new DialogInterface.OnDismissListener()
		{

			@Override
			public void onDismiss(DialogInterface dialog)
			{
				if (!gaveCorrectPassword)
				{
					executeIfFailed.run();
				}
			}
		});


		gaveCorrectPassword = false;

		builder.show();
	}

	public static byte[] getHash(String toHash)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(toHash.getBytes("UTF-8"));
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
			//this shouldn't happen unless the hardcoded strings are wrong
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Checks if the provided password is correct, and saves the hash into lastPassword
	 * @param correctPassword the sha256 sum of the correct password
	 * @param enteredPassword the plaintext entered password
	 */
	private static boolean checkPassword(byte[] correctPassword, String enteredPassword)
	{
		byte[] enteredHash = getHash(enteredPassword);
		lastPasswordHash = enteredHash;

		return Arrays.equals(correctPassword, enteredHash);
	}

	/**
	 * Call this method from the activity's onSaveInstanceState method so that the password dialog doesn't
	 * re-popup when the device is rotated.
	 * @param toSaveTo
	 * @return
	 */
	public static void onSaveInstanceState(Bundle toSaveTo)
	{
		if(lastPasswordHash != null)
		{
			toSaveTo.putString("lastPassword", Base64.encodeToString(PasswordDialog.lastPasswordHash, Base64.NO_WRAP));
		}
	}
}
