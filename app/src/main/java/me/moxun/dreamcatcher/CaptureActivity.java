package me.moxun.dreamcatcher;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.security.KeyChain;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.dd.morphingbutton.MorphingButton;
import com.dd.morphingbutton.impl.IndeterminateProgressButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InputStream;

import me.moxun.dreamcatcher.event.OperateEvent;
import me.moxun.dreamcatcher.service.ProxyService;

public class CaptureActivity extends AppCompatActivity {

    final String CA_RESOURCE = "/sslSupport/ca-certificate-rsa.cer";
    final int INSTALL_CA_REQUEST_CODE = 0x99;

    private int size = 112;
    private IndeterminateProgressButton controlButton;
    private TextView status;
    private int state = State.IDLE;
    private long timestamp = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_capture);

        EventBus.getDefault().register(this);

        controlButton = (IndeterminateProgressButton) findViewById(R.id.controller);
        status = (TextView) findViewById(R.id.status);

        morphToIdle(controlButton, 0);

        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (state) {
                    case State.IDLE:
                    case State.FAILURE:
                        timestamp = System.currentTimeMillis();
                        performProgress(controlButton);
                        Intent intent = new Intent(CaptureActivity.this, ProxyService.class);
                        startService(intent);
                        break;
                    case State.RUNNING:
                        timestamp = System.currentTimeMillis();
                        performProgress(controlButton);
                        stopService(new Intent(CaptureActivity.this, ProxyService.class));
                        break;
                    default:
                        break;
                }
            }
        });

        findViewById(R.id.install_ca).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    byte[] keychainBytes;
                    InputStream bis = CaptureActivity.class.getResourceAsStream(CA_RESOURCE);
                    keychainBytes = new byte[bis.available()];
                    bis.read(keychainBytes);
                    Intent intent = KeyChain.createInstallIntent();
                    intent.putExtra(KeyChain.EXTRA_CERTIFICATE, keychainBytes);
                    intent.putExtra(KeyChain.EXTRA_NAME, "DreamCatcher CA Certificate");
                    startActivityForResult(intent, INSTALL_CA_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.trusted_ca).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.android.settings.TRUSTED_CREDENTIALS_USER");
                intent.setFlags(0x14000000);
                startActivity(intent);
            }
        });

        findViewById(R.id.setting_proxy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
    }

    private void performProgress(@NonNull final IndeterminateProgressButton button) {
        status.setText("This maybe a bit slow, please keep patience ……");
        int progressColor1 = Color.parseColor("#ff00ddff");
        int progressColor2 = Color.parseColor("#ff99cc00");
        int progressColor3 = Color.parseColor("#ffffbb33");
        int progressColor4 = Color.parseColor("#ffff4444");
        int color = Color.parseColor("#ffdedede");
        int progressCornerRadius = $px(4);
        int width = $px(200);
        int height = $px(8);

        button.blockTouch();
        button.morphToProgress(color, progressCornerRadius, width, height, 500, progressColor1, progressColor2,
                progressColor3, progressColor4);
    }

    private int $px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void morphToSuccess(final IndeterminateProgressButton btnMorph, int duration) {
        status.setText("Proxy on 127.0.0.1:" + ((DCApplication) getApplication()).getPort());
        btnMorph.unblockTouch();
        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius($px(size))
                .width($px(size))
                .height($px(size))
                .color(Color.parseColor("#ff99cc00"))
                .colorPressed(Color.parseColor("#ff6d9b00"))
                .icon(R.mipmap.ic_check);
        btnMorph.morph(circle);
    }

    private void morphToFailure(final IndeterminateProgressButton btnMorph, int duration) {
        btnMorph.unblockTouch();
        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius($px(size))
                .width($px(size))
                .height($px(size))
                .color(Color.parseColor("#ffff4444"))
                .colorPressed(Color.parseColor("#ffcd3a3a"))
                .icon(R.mipmap.ic_closed);
        btnMorph.morph(circle);
    }

    private void morphToIdle(final IndeterminateProgressButton btnMorph, int duration) {
        btnMorph.unblockTouch();
        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius($px(size))
                .width($px(size))
                .height($px(size))
                .color(Color.parseColor("#ff0099cc"))
                .colorPressed(Color.parseColor("#ff00719b"))
                .icon(R.mipmap.ic_start);
        btnMorph.morph(circle);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final OperateEvent event) {
        Log.e("Event", event.toString());
        if (event.error) {
            if ((System.currentTimeMillis() - timestamp) < 3000) {
                getWindow().getDecorView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        morphToFailure(controlButton, 500);
                        state = State.FAILURE;
                        status.setText(event.msg);
                    }
                }, 3000 - (System.currentTimeMillis() - timestamp));
            } else {
                morphToFailure(controlButton, 500);
                state = State.FAILURE;
                status.setText(event.msg);
            }
            return;
        }

        if (event.target == OperateEvent.TARGET_CONNECTOR) {

        } else if (event.target == OperateEvent.TARGET_PROXY) {
            if (event.active) {
                if ((System.currentTimeMillis() - timestamp) < 3000) {
                    getWindow().getDecorView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            morphToSuccess(controlButton, 500);
                            state = State.RUNNING;
                        }
                    }, 3000 - (System.currentTimeMillis() - timestamp));
                } else {
                    morphToSuccess(controlButton, 500);
                    state = State.RUNNING;
                }
            } else {
                if ((System.currentTimeMillis() - timestamp) < 3000) {
                    getWindow().getDecorView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            morphToIdle(controlButton, 500);
                            state = State.IDLE;
                            status.setText("Click to start capture");
                        }
                    }, 3000 - (System.currentTimeMillis() - timestamp));
                } else {
                    morphToIdle(controlButton, 500);
                    state = State.IDLE;
                    status.setText("Click to start capture");
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
