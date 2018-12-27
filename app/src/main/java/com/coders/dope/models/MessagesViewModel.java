package com.coders.dope.models;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.coders.dope.database.entity.ChatMessage;
import com.coders.dope.repositories.MessagesRepository;
import com.coders.dope.repositories.NewMessagesObserver;
import com.coders.dope.utils.LoggerDebug;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class MessagesViewModel extends ViewModel implements NewMessagesObserver {
    private static final String TAG = MessagesViewModel.class.getSimpleName();
    private MutableLiveData<List<ChatMessage>> chatMessages;
    private MutableLiveData<String> statusLiveData;

    private MessagesRepository messagesRepository;
    private String username;

    @Inject
    public MessagesViewModel(MessagesRepository messagesRepository) {
        this.messagesRepository = messagesRepository;
        init();
        LoggerDebug.print("ViewModel initialized", TAG);
        messagesRepository.register(this);
    }

    private void init() {
        if (chatMessages == null) {
            Log.d(TAG, "FirstTime loading chatMessages");
            chatMessages = new MutableLiveData<>();
            statusLiveData = new MutableLiveData<>();
            if (messagesRepository.getInitialMessages() != null) {//Fix the bug: When you pressed back and return to the chat the recycler view doesnt populate again.
                chatMessages.setValue(messagesRepository.getInitialMessages());
            } else {
                chatMessages.setValue(new ArrayList<>());
            }
        }
    }

    /**
     * Get Live Data that containts ArrayList With All the Messages
     */

    public MutableLiveData<List<ChatMessage>> getChatMessages() {

        // Log.d(TAG, "Sending Messages: " + chatMessages.getValue().size());

        return chatMessages;
    }


    /**
     * Adding message to the LiveData Object.
     *
     * @param chatMessage Message to Add to LiveData from Repo
     */

    private void addMessageFromRepository(ChatMessage chatMessage) {
        LoggerDebug.print("New Message: " + chatMessage.toString(), TAG);

        Objects.requireNonNull(chatMessages.getValue()).add(chatMessage);
        chatMessages.postValue(chatMessages.getValue()); //Because the thread are sending in background thread
        Log.d(TAG, "Message Added, total Messages: " + chatMessages.getValue().size());


    }


    /**
     * Getting a new Message from repository
     *
     * @param newChatMessage: ChatMessage from AVA.
     */
    @Override
    public void update(ChatMessage newChatMessage) {
        LoggerDebug.printMessageTrace("Message from ava: " + newChatMessage.getMessage(), TAG, LoggerDebug.MODE_RECEIVING);
        addMessageFromRepository(newChatMessage);
    }

    /**
     * Get All in -local database chat messages.
     */


    @Override
    public void loadInitialMessages(List<ChatMessage> chatMessages) {
        LoggerDebug.print("Loading Initial Messages, ", TAG);
        this.chatMessages.postValue(chatMessages);
        for (int i = 0; i < chatMessages.size(); i++) {
            LoggerDebug.print(chatMessages.get(i).getMessage(), TAG);
        }
    }

    @Override
    public void updateChatStatus(String socketStatus) {
        LoggerDebug.print("ChatStatus: " + socketStatus, TAG);
        statusLiveData.postValue(socketStatus);
    }

    @Override
    public void initialState(String status) {
        statusLiveData.postValue(status);
    }


    public void setUsername(String username) {
        this.username = username;
        messagesRepository.setmUsername(username);
    }

    /**
     * Send a message to the repository, connecting UI with MessagesRepo.
     *
     */

    public void sendMessage(ChatMessage message) {
        messagesRepository.sendMessage(message);
        Objects.requireNonNull(chatMessages.getValue()).add(message);
        chatMessages.postValue(chatMessages.getValue());
        LoggerDebug.printMessageTrace(message.getMessage(), TAG, LoggerDebug.MODE_SENDING);
    }


    public MutableLiveData<String> getStatusLiveData() {
        return statusLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        messagesRepository.delete(this);
    }
}
