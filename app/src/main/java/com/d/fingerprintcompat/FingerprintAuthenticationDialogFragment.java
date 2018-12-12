package com.d.fingerprintcompat;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.d.lib.fingerprintcompat.FingerprintCompat;
import com.d.lib.fingerprintcompat.base.FingerprintException;
import com.d.lib.fingerprintcompat.base.IFingerprint;

/**
 * A dialog which uses fingerprint APIs to authenticate the user.
 */
public class FingerprintAuthenticationDialogFragment extends DialogFragment {
    private static final long ERROR_TIMEOUT_MILLIS = 1600;

    private Context mContext;
    private ImageView mIcon;
    private TextView mTips;
    private FingerprintCompat mFingerprintCompat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        mContext = getActivity();
        mFingerprintCompat = FingerprintCompat.create(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.sign_in));
        View root = inflater.inflate(R.layout.dialog_fingerprint, container, false);
        mIcon = (ImageView) root.findViewById(R.id.iv_fingerprint_icon);
        mTips = (TextView) root.findViewById(R.id.tv_fingerprint_status);
        Button btnCancel = (Button) root.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        Button btnOk = (Button) root.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return root;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        mIcon.setImageResource(R.drawable.ic_fingerprint);
        mFingerprintCompat.authenticate(new IFingerprint.Callback() {

            @Override
            public void onHelp(int helpMsgId, CharSequence helpString) {
                if (helpMsgId == FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST) {
                    showHelp(helpString);
                }
            }

            @Override
            public void onSuccess(String value) {
                showSuccess();
            }

            @Override
            public void onError(@NonNull FingerprintException e) {
                showError(e.desc);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintCompat.cancel();
    }

    private void showSuccess() {
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mTips.removeCallbacks(mResetErrorTextRunnable);
        mTips.setTextColor(ContextCompat.getColor(mContext, R.color.color_success));
        mTips.setText(mContext.getResources().getString(R.string.fingerprint_success));
    }

    private void showError(CharSequence error) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mTips.setText(error);
        mTips.setTextColor(ContextCompat.getColor(mContext, R.color.color_error));
        mTips.removeCallbacks(mResetErrorTextRunnable);
        mTips.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private void showHelp(CharSequence help) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_warning);
        mTips.setText(help);
        mTips.setTextColor(ContextCompat.getColor(mContext, R.color.color_warning));
        mTips.removeCallbacks(mResetErrorTextRunnable);
        mTips.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mTips.setTextColor(ContextCompat.getColor(mContext, R.color.color_hint));
            mTips.setText(mTips.getResources().getString(R.string.fingerprint_hint));
            mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    };
}
