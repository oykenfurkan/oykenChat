package com.furkan.profil.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;

import com.furkan.profil.Helpers.RSAHelper;
import com.furkan.profil.R;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageDetails extends AppCompatActivity {


    @BindView(R.id.editText3)
    EditText message;
    @BindView(R.id.editText4)
    EditText receiversPublicKey;
    @BindView(R.id.editText6)
    EditText timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);
        ButterKnife.bind(this);

        message.setText(getIntent().getStringExtra("message"));
        receiversPublicKey.setText(getIntent().getStringExtra("receiverPubKey"));

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.valueOf(getIntent().getStringExtra("timestamp")));
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        timestamp.setText(String.valueOf(date));
    }

}