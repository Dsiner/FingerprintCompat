package com.d.fingerprintcompat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final String DIALOG_FRAGMENT_TAG = "fingerprintFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
    }

    private void bindView() {
        findViewById(R.id.btn_purchase_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerprintAuthenticationDialogFragment fragment
                        = new FingerprintAuthenticationDialogFragment();
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        });
    }
}
