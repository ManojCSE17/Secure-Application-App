package com.example.secureapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    // public static final String TAG = "TAG";
    EditText mEmail,mPassword;
    Button mbutton_log;
    TextView mto_register,mforgotPassword;
    FirebaseAuth fAuth;
    int alertcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        alertcount = 4;

        mEmail                 =    findViewById(R.id.mail);
        mPassword              =    findViewById(R.id.password);
        mbutton_log            =    findViewById(R.id.button_log);
        mto_register           =    findViewById(R.id.to_register);
        mforgotPassword        =    findViewById(R.id.forgotPassword);

        fAuth = FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        mbutton_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required.");
                    return;
                } else {
                    Pattern pe = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$");
                    if (!(pe.matcher(email).matches())) {
                        mEmail.setError("Enter correct email.");
                        return;
                    }
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required.");
                    return;
                } else {
                    password = password.replaceAll("[^0-9a-zA-Z@#$%^&+=]","");
                }

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                AlertDialog.Builder mprog = new AlertDialog.Builder(v.getContext());
                View mpview = getLayoutInflater().inflate(R.layout.progresslayout, null);
                mprog.setView(mpview);
                final AlertDialog progressdialog = mprog.create();
                progressdialog.setCanceledOnTouchOutside(false);
                progressdialog.show();

                final AlertDialog.Builder malert = new AlertDialog.Builder(v.getContext());

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressdialog.dismiss();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            progressdialog.dismiss();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(Login.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            View mview = getLayoutInflater().inflate(R.layout.alertdialog, null);
                            final TextView mstring = (TextView) mview.findViewById(R.id.alert);
                            alertcount = alertcount - 1;

                            if(alertcount != 0){

                                mstring.setText("You have only "+alertcount+" attempts.");
                                Button mok = (Button) mview.findViewById(R.id.ok);
                                malert.setView(mview);
                                final AlertDialog alertdialog = malert.create();
                                alertdialog.setCanceledOnTouchOutside(false);
                                alertdialog.show();

                                mok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertdialog.dismiss();
                                    }
                                });

                            }
                            else{
                                finish();
                            }
                        }

                    }
                });

            }

            });

        mto_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Register.class));
                finish();
            }
        });

        mforgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder mbuilder = new AlertDialog.Builder(v.getContext());
                View mView = getLayoutInflater().inflate(R.layout.resetdialog, null);
                final EditText mcEmail = (EditText) mView.findViewById(R.id.emailid);
                Button msend = (Button) mView.findViewById(R.id.send);

                mbuilder.setView(mView);
                final AlertDialog dialog = mbuilder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                msend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String cmail = mcEmail.getText().toString();
                        if (TextUtils.isEmpty(cmail)) {
                            mcEmail.setError("Email is Required.");
                            return;
                        } else {
                            Pattern pe = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$");
                            if (!(pe.matcher(cmail).matches())) {
                                mcEmail.setError("Enter correct email.");
                                return;
                            }
                        }
                        fAuth.sendPasswordResetEmail(cmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error ! Reset Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                    }
                });
            }
            });

    }
}


