package com.example.notebooktest.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notebooktest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText ;
    private FirebaseFirestore db;
    private Button signUpButton;
    private boolean emailOk, passwordOk;
    private FirebaseAuth firebaseAuth;
    private ImageView showHideButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailEditText = findViewById(R.id.edit_text_email);
        emailEditText.addTextChangedListener(new EditTextWatcher(emailEditText));

        passwordEditText = findViewById(R.id.edit_text_password);
        passwordEditText.addTextChangedListener(new EditTextWatcher(passwordEditText));



        signUpButton = findViewById(R.id.button_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        showHideButton = findViewById(R.id.show_hide_button);
        showHideButton.setOnClickListener(v -> {
            if(passwordEditText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                showHideButton.setImageResource(R.drawable.ic_show_password);
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            else{
                showHideButton.setImageResource(R.drawable.ic_hide_password);
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        TextView linkToSignIn = findViewById(R.id.link_to_signin);
        linkToSignIn.setText(Html.fromHtml(getString(R.string.don_t_have_account) + "<b><font color=#D32F2F>" + getString(R.string.sign_in) + "</font></b>"));
        linkToSignIn.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });


        if (isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            return;
        }


        signUpButton.setOnClickListener(v -> {

            if (isNetworkAvailable()){
                Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                return;
            }

            final String email = emailEditText.getText().toString().trim();
            final String password = passwordEditText.getText().toString().trim();

            signUpwithEmailandPassword(email, password);
        });

    }



    private void addUserToDb() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user!=null) {
            Map<String, Object> userDoc = new HashMap<>();
            userDoc.put("uid", user.getUid());
            userDoc.put("email", user.getEmail());

            db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                    .set(userDoc)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            firebaseAuth.getCurrentUser().sendEmailVerification();
                            startActivity(new Intent(SignUpActivity.this, MyNotesActivity.class));
                            finish();
                        }
                    });
        }

    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void checkButton(){
        signUpButton.setEnabled(passwordOk && emailOk);
    }
    private void signUpwithEmailandPassword(final String email,final String password){

        emailEditText.clearFocus();
        passwordEditText.clearFocus();
        signUpButton.setEnabled(false);

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
                        if(firebaseAuth.getCurrentUser()!=null) {
                            addUserToDb();
                        }

                    }else{

                        if (task.getException() instanceof FirebaseAuthUserCollisionException){
                            emailEditText.setError("Email is already in use");
                            emailEditText.requestFocus();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                            emailEditText.setError("Email is not valid");
                            emailEditText.requestFocus();
                        } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            passwordEditText.setError("Password is weak");
                            passwordEditText.requestFocus();
                        }else{
                            Toast.makeText(SignUpActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo == null || !activeNetworkInfo.isConnected();
    }

    private class EditTextWatcher implements TextWatcher {

        private final View view;
        private EditTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @SuppressLint("NonConstantResourceId")
        public void afterTextChanged(Editable editable) {
            switch(view.getId()){

                case R.id.edit_text_email:
                    emailOk = false;
                    if (emailEditText.getText().toString().trim().length()>6){
                        if (isEmailValid(emailEditText.getText().toString().trim())) {
                            emailOk = true;
                        }else{
                            emailOk = false;
                            emailEditText.setError("Email is not valid");
                        }
                        checkButton();
                    }
                    break;

                case R.id.edit_text_password:
                    passwordOk = false;
                    final String password = passwordEditText.getText().toString().trim();
                    if (password.length()<6){
                        passwordEditText.setError("Password should be at least 6 char");
                    }else{
                        passwordOk = true;
                    }
                    checkButton();

                    break;
            }

        }
    }
}
