package com.example.secureapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    TextView Name,email,phone;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    String userId;
    long var;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Name  = findViewById(R.id.profileName);
        email = findViewById(R.id.profileMail);
        phone = findViewById(R.id.profilephone);

        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = db.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    Name.setText(documentSnapshot.getString("Name"));
                    email.setText(documentSnapshot.getString("email"));
                    var = documentSnapshot.getLong("Phone");
                    phone.setText(Long.toString(var));
                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });
    }

    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
}
