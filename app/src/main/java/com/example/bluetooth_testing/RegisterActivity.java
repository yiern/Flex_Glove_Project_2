package com.example.bluetooth_testing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText etAge = (EditText) findViewById(R.id.etAge);
        final EditText etName = (EditText) findViewById(R.id.etName);
        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final EditText etDoctor = (EditText) findViewById(R.id.etDoctor);
        final Button bRegister = (Button) findViewById(R.id.bRegister);
        final EditText etEmail = (EditText) findViewById(R.id.etEmail);
        final EditText etPhone = (EditText) findViewById(R.id.etPhoneNumber);


        final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        final String phonePattern = "^\\+[0-9]{10,13}$";
        final String TAG = "register Activity";

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          try {
              final String name = etName.getText().toString();
              final String username = etUsername.getText().toString();
              final int age = Integer.parseInt(etAge.getText().toString());
              final String doctor_name = etDoctor.getText().toString();
              final String password = etPassword.getText().toString();
              final String Email = etEmail.getText().toString();
              final String phone = etPhone.getText().toString();

              if (Patterns.EMAIL_ADDRESS.matcher(Email).matches() && phone.length() == 8) {
                  Response.Listener<String> responseListener = new Response.Listener<String>() {
                      @Override
                      public void onResponse(String response) {
                          try {
                              JSONObject jsonResponse = new JSONObject(response);
                              boolean success = jsonResponse.getBoolean("success");
                              if (success) {
                                  Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                  RegisterActivity.this.startActivity(intent);
                              } else {
                                  AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                  builder.setMessage("Register Failed")
                                          .setNegativeButton("Retry", null)
                                          .create()
                                          .show();
                              }
                          } catch (JSONException e) {
                              e.printStackTrace();
                          }
                      }
                  };

                  RegisterRequest registerRequest = new RegisterRequest(name, username, age, password, doctor_name, Email, phone, responseListener);
                  RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                  queue.add(registerRequest);

                  Toast.makeText(RegisterActivity.this, "Successfully created account", Toast.LENGTH_SHORT).show();

              } else
                  {
                      if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches())
                      {


                          String errorString = "incorrect format";

                          etEmail.setError(errorString);
                      }
                      if (phone.length() != 8)
                      {

                          String errorString = "invalid number";

                          etPhone.setError(errorString);
                      }

                  }

          }catch (NumberFormatException e)
          {
              Log.d(TAG, "onClick: invalid number exception");
              Toast.makeText(RegisterActivity.this, "Please complete the fields",Toast.LENGTH_SHORT).show();
          }


            }
        });
    }
}