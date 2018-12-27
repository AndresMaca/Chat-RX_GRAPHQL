package com.coders.dope.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.coders.dope.Adapter.MessageAdapter;
import com.coders.dope.database.entity.ChatMessage;
import com.coders.dope.models.MessagesViewModel;
import com.coders.dope.utils.LoggerDebug;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import static com.coders.dope.repositories.ChatStatus.CHAT_CONNECTED;

public class MainActivity extends AppCompatActivity {
    private RecyclerView messageRecyclerView;
    private MessageAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutMananger;
    private Button sendButton;
    private EditText textBox;
    private int mAlerterHeight;
    private TextView tvAlert;
    private static final long ALERT_LENGTH = 2000;
    private String status;


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String USER_NAME = "huaweiP20";

    private ArrayList<ChatMessage> data;
    @Inject
    ViewModelProvider.Factory factory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity_main);
        configureDagger();

        messageRecyclerView = findViewById(R.id.reyclerview_message_list);
        if (data == null)
            data = new ArrayList<>();
        recyclerAdapter = new MessageAdapter(data);
        LoggerDebug.print("onCreate", TAG);
        LoggerDebug.print("onCreate data size: " + data.size(), TAG);


        layoutMananger = new LinearLayoutManager(this);

        messageRecyclerView.setLayoutManager(layoutMananger);

        messageRecyclerView.setAdapter(recyclerAdapter);
        messageRecyclerView.scrollToPosition(recyclerAdapter.getItemCount() - 1);
        MessagesViewModel messagesViewModel =
                ViewModelProviders.of(this, factory).get(MessagesViewModel.class);
        messagesViewModel.setUsername(USER_NAME); //TODO get UserName From UI
        /*
         * Observer that view the latest added message in the repository, so when a new message arrives the UI can be updated.
         */

        Observer<List<ChatMessage>> messagesObserver = chatMessages -> {
            try {
                if (data.size() == 0) {
                    data.addAll(chatMessages);
                } else {
                    assert chatMessages != null;
                    data.add(chatMessages.get(chatMessages.size() - 1));
                }

                recyclerAdapter.notifyDataSetChanged();
                messageRecyclerView.scrollToPosition(recyclerAdapter.getItemCount() - 1);
                LoggerDebug.print("New 1 Message!, data size: , " + data.size(), TAG);

            } catch (IndexOutOfBoundsException exception) {
                LoggerDebug.print("Error!: " + exception, TAG);
            }

        };
        messagesViewModel.getChatMessages().observe(this, messagesObserver);
        sendButton = findViewById(R.id.button_chatbox_send);
        textBox = findViewById(R.id.edittext_chatbox);
        tvAlert = findViewById(R.id.tvAlert);
        sendButton.setOnClickListener(view -> {
            Log.d(TAG, "send Clicked");
            if (!textBox.getText().toString().equals("")) {
                messagesViewModel.sendMessage(new ChatMessage(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()),
                        true, textBox.getText().toString(), USER_NAME, "date", "image_url", "static"));
                LoggerDebug.printMessageTrace(textBox.getText().toString(), TAG, LoggerDebug.MODE_SENDING);
                LoggerDebug.print("Data Arraylist with: " + data.size() + "Messages", TAG);
            }

        });
        mAlerterHeight = tvAlert.getHeight();

        messagesViewModel.getStatusLiveData().observe(this, s -> {
            LoggerDebug.print("Chat Status: " + s, TAG);
            if (s != null && !s.equals("")) {
                tvAlert.setText(s);

                if (s.equals(CHAT_CONNECTED)) {
                    tvAlert.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSuccess));
                    showAlert(true);


                } else {
                    tvAlert.setBackgroundColor(ContextCompat.getColor(this, R.color.colorError));
                    showAlert(false);
                }


            }
        });


    }

    /*
    Pops and re start view
     */
    private void showAlert(boolean connected) {
        mAlerterHeight = tvAlert.getHeight();
        LoggerDebug.print("height: " + mAlerterHeight, TAG);
        tvAlert.setTranslationY(-1 * mAlerterHeight); //Lo oculta sin animación.
        tvAlert.animate()
                .translationY(0)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                LoggerDebug.print("height: " + mAlerterHeight, TAG);
                                if (connected) hideAlert();
                            }
                        }, ALERT_LENGTH);
                    }
                });

    }

    private void hideAlert() {
        tvAlert.animate()
                .translationY(-1 * mAlerterHeight)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvAlert.setText("");
                        LoggerDebug.print("height: " + mAlerterHeight, TAG);

                    }
                });
    }

    /**
     * Configure Dagger to get ready for injections
     */
    private void configureDagger() {
        AndroidInjection.inject(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
