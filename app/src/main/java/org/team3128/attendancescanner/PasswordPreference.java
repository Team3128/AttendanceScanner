package org.team3128.attendancescanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Preference option for setting the password.
 */
public class PasswordPreference extends DialogPreference implements TextWatcher
{

	TextView oldPassText;
	TextView newPassText;
	TextView confirmPassText;

	SharedPreferences preferences;

	String password;

	Button positiveButton; //can only be set after the dialog has been shown.

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
	{
		super.onPrepareDialogBuilder(builder);
		builder.setNeutralButton(R.string.no_password, this);
	}

	public PasswordPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setTitle(R.string.change_password);
		preferences = getSharedPreferences();
		password = preferences.getString(PasswordDialog.PASSWORD_HASH_PREFS_KEY, "");

		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setIcon(android.R.drawable.ic_menu_preferences);
		setLayoutResource(R.layout.dialog_change_password);
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		switch(which)
		{
			case DialogInterface.BUTTON_POSITIVE:
				byte[] newPassBytes = PasswordDialog.getHash(newPassText.getText().toString());

				preferences.edit().putString("password", Base64.encodeToString(newPassBytes, Base64.NO_WRAP)).apply();

				//update cached password
				PasswordDialog.lastPasswordHash = newPassBytes;
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				preferences.edit().putString("password", "").apply();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				//do nothing
		}

	}

	@Override
	protected View onCreateDialogView()
	{
		View content = super.onCreateDialogView();

		oldPassText = (TextView) content.findViewById(R.id.oldPassInputText);
		newPassText = (TextView) content.findViewById(R.id.newPassInputText);
		confirmPassText = (TextView) content.findViewById(R.id.newPassConfirmText);

		if(password.isEmpty())
		{
			oldPassText.setEnabled(false);
		}

		return content;
	}

	@Override
	protected void showDialog(Bundle state)
	{
		super.showDialog(state);

		//populate our button class variable
		positiveButton = ((AlertDialog)getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
		positiveButton.setEnabled(false);

		oldPassText.addTextChangedListener(this);
		newPassText.addTextChangedListener(this);
		confirmPassText.addTextChangedListener(this);

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		//do nothing
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		//do nothing
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		String oldPassword = oldPassText.getText().toString();
		String newPassword = newPassText.getText().toString();
		String confirmPassword = confirmPassText.getText().toString();

		boolean passwordsMatch = !newPassword.isEmpty() && !confirmPassword.isEmpty() && newPassword.equals(confirmPassword);

		boolean oldPasswordCorrect;


		if(password.isEmpty())
		{
			oldPasswordCorrect = true;
		}
		else
		{
			byte[] currPasswordBytes = Base64.decode(password, Base64.NO_WRAP);

			oldPasswordCorrect = PasswordDialog.doPasswordsMatch(currPasswordBytes, oldPassword);
		}

		positiveButton.setEnabled(passwordsMatch && oldPasswordCorrect);
	}
}
