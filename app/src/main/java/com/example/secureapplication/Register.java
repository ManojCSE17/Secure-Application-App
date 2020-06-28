package com.example.secureapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText mName,mEmail,mPassword,mconfirm_password,mphone;
    Button mbutton_submit;
    TextView mto_login;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName                  =    findViewById(R.id.Name);
        mEmail                 =    findViewById(R.id.mail);
        mPassword              =    findViewById(R.id.password);
        mconfirm_password      =    findViewById(R.id.confirm_password);
        mbutton_submit         =    findViewById(R.id.button_submit);
        mto_login              =    findViewById(R.id.to_login);
        mphone                 =    findViewById(R.id.Phone);

        fAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }


        mbutton_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email   =   mEmail.getText().toString().trim();
                String password      =   mPassword.getText().toString().trim();
                final String name    =   mName.getText().toString();
                String confirm       =   mconfirm_password.getText().toString();
                final String phone   =   mphone.getText().toString();
                final long Phone     =   Long.parseLong(phone);

                if(TextUtils.isEmpty(name)){
                    mName.setError("Name is Required.");
                    return;
                }
                else {
                    Pattern pn = Pattern.compile("^[A-Za-z]+$");
                    if(!(pn.matcher(name).matches())) {
                        mName.setError("Enter correct name.");
                        return;
                    }
                }

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }
                else {
                    Pattern pe = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$");
                    if (!(pe.matcher(email).matches())) {
                        mEmail.setError("Enter correct email.");
                        return;
                    }
                }

                if(TextUtils.isEmpty(phone)){
                    mphone.setError("Phone Number is Required.");
                    return;
                }
                else {
                    Pattern pn = Pattern.compile("^[6-9][0-9]{9}$");
                    if (!(pn.matcher(phone).matches())) {
                        mphone.setError("Enter correct phone number.");
                        return;
                    }
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }
                else {
                    if (password.length() < 8) {
                        mPassword.setError("Password Must be >= 8 Characters and <= 20 Characters");
                        return;
                    }
                    String regex = "^(?=.*[0-9])"
                            + "(?=.*[a-z])(?=.*[A-Z])"
                            + "(?=.*[@#$%^&+=])"
                            + "(?=\\S+$).{8,20}$";
                    Pattern pp = Pattern.compile(regex);
                    if(!(pp.matcher(password).matches())) {
                        mPassword.setError("Enter correct password format");
                        return;
                    }
                }

                if(TextUtils.isEmpty(confirm)){
                    mconfirm_password.setError("Confirm Password is Required.");
                    return;
                }
                else {
                    int ch = confirm.compareTo(password);
                    if (ch != 0) {
                        mconfirm_password.setError("Password Must match!");
                        return;
                    }
                }

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                AlertDialog.Builder mprog = new AlertDialog.Builder(v.getContext());
                View mpview = getLayoutInflater().inflate(R.layout.progresslayout, null);
                mprog.setView(mpview);
                final AlertDialog progressdialog = mprog.create();
                progressdialog.setCanceledOnTouchOutside(false);
                progressdialog.show();

                // register the user in firebase

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // send verification link
                            progressdialog.dismiss();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            FirebaseUser fuser = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Register.this, "Verification mail Has been sent to your Email.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Email not sent " + e.getMessage());
                                }
                            });

                            Toast.makeText(Register.this, "User Created.", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = db.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("Name",name);
                            user.put("email",email);
                            user.put("Phone",Phone);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: user Profile is created for "+ userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }else {
                            progressdialog.dismiss();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(Register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        mto_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });

    }
}


