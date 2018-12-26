package com.coders.dope.repositories;

import android.util.Log;

import com.apollographql.apollo.ApolloCall.Callback;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.exception.ApolloHttpException;
import com.apollographql.apollo.exception.ApolloNetworkException;
import com.apollographql.apollo.sample.AboutUsQuery;
import com.apollographql.apollo.sample.CreateUserMutation;
import com.apollographql.apollo.sample.DeleteThisUserMutation;
import com.apollographql.apollo.sample.MutationMessageMutation;
import com.apollographql.apollo.sample.MutationMessageMutation.Data;
import com.apollographql.apollo.sample.SubscriptionToBotSubscription;
import com.apollographql.apollo.sample.type.CreateUserInput;
import com.apollographql.apollo.sample.type.DeleteUserInput;
import com.apollographql.apollo.sample.type.MessageInput;
import com.coders.dope.database.dao.MessageDao;
import com.coders.dope.database.entity.ChatMessage;
import com.coders.dope.utils.LoggerDebug;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;


/*
Bidirectional Com.
    Validate User √
 */

@Singleton
public class MessagesRepository implements NewMessageSubject, ApolloConnection {

    private static final String TAG = MessagesRepository.class.getSimpleName();
    private final Executor executor;
    private final MessageDao messageDao;
    private final ApolloClient apolloClient;
    private String mUsername = "usernameAndroid";
    private String userID;
    private boolean isListening;
    private List<NewMessagesObserver> observers;
    private List<ChatMessage> initialMessages;
    private static long longRetryTime  = 5000L;
    private static long shortRetryTime = 500L;

    private String status = "";
    //TODO unit testing
    //TODO with RxComponents.
    // Some ideas make an implementation of MessageViewModel, pass to ModelView  and from MView to MainActivity so it can manange the lifecycle of Sockets
    @Inject
    public MessagesRepository(Executor executor, MessageDao messageDao, ApolloClient apolloClient) {
        this.executor = executor;
        this.messageDao = messageDao;
        this.apolloClient = apolloClient;
        observers = new ArrayList<>();
        getInitialData();
        connectWithServer();
        rxInit();

    }
    private void rxInit(){

    }
    @Override
    public void register(NewMessagesObserver observer) {
        LoggerDebug.print("observers initialized", TAG);
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void delete(NewMessagesObserver observer) {
        observers.remove(observer);
        //TODO idea when there are not any observer destroy this
        if (observers.size() == 0) {
            LoggerDebug.print("ObserverSize = 0", TAG);

        }
    }

    /**
     * Sends the message to all the observers.
     *
     * @param chatMessage
     */
    @Override
    public void notifyNewMessagesObserver(ChatMessage chatMessage) { //Notifying that new message from ava is here!
        LoggerDebug.print("Notifying the observers", TAG);
        for (final NewMessagesObserver observer : observers) {
            observer.update(chatMessage);
        }
    }

    @Override
    public void notifyInitialMessages(List<ChatMessage> chatMessages) {
        this.initialMessages = chatMessages;
        LoggerDebug.print("Getting initial Data", TAG);
        for (final NewMessagesObserver observer : observers) {
            observer.loadInitialMessages(chatMessages);
        }

    }

    @Override
    public void notifyStatus(String socketStatus) {
        LoggerDebug.print("Socket Status: " + socketStatus, TAG);
        for (final NewMessagesObserver observer : observers) {
            observer.updateSocketStatus(socketStatus);
        }

    }


    public void setmUsername(String mUsername) {//TODO hacer obligatorio poner el username
        this.mUsername = mUsername;
    }

    public void connectWithServer() {
        if (!userIdExists())
            getUserID();


    }

    /**
     * Get User Id and start Listening!
     */
    private void getUserID() {
        apolloClient.mutate(CreateUserMutation.builder().user(CreateUserInput.builder().username(mUsername).build()).build()).enqueue(new Callback<CreateUserMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<CreateUserMutation.Data> response) {
                if (response.data() != null) {
                    userID = response.data().newUser().userId();
                    startListening();
                    queryTest();
                    LoggerDebug.print("UserID: " + userID, TAG);
                } else {
                    LoggerDebug.print("cant get userID", TAG);
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                if (e.getMessage().equals("Failed to execute http call")) {
                    LoggerDebug.print("No network, trying again!", TAG);
                    LoggerDebug.print(e.getMessage() + " more: " + e.toString(), TAG);
                    executor.execute(() -> {
                        connectWithServer();
                        waitsFunction(longRetryTime);
                    });

                }
            }
        });
    }

    public void startListening() {
        if (userIdExists()) {
            SubscriptionToBotSubscription subscriptionToBotSubscription = SubscriptionToBotSubscription.builder().userID(userID).build();
            ApolloSubscriptionCall apolloSubscriptionCall = apolloClient.subscribe(subscriptionToBotSubscription).clone();
            apolloSubscriptionCall.execute(new ApolloSubscriptionCall.Callback() {
                @Override
                public void onResponse(@NotNull Response response) {
                    if (response.data() != null) {
                        isListening = true;
                        Log.d(TAG, "Response from listener: " + response.data().toString());
                        addMessage(mUsername, extractMessage(response.data().toString()), false);
                    } else
                        LoggerDebug.print(TAG, "Response is null!!");
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LoggerDebug.print(TAG, "Error with subscriptions: " + e.toString());
                    LoggerDebug.print("Error: " + e.getMessage(), TAG);
                    isListening = false;
                    // WARNING THE NEXT LINE IS VERY DANGEROUS BECAUSE THE TYPO CAN CHANGE!
                    if (e.getMessage().contains("Already subscribed")) {
                        LoggerDebug.print("Already subscribed, setting to listening", TAG);
                        isListening = true;
                    }

                }


                @Override
                public void onCompleted() {
                    LoggerDebug.print("OnCompleted Subscription Method", TAG);


                }
            });
        } else {
            getUserID();

        }

    }

    /**
     * Executes only in background threads!
     */
    private void waitsFunction(Long time) {
        LoggerDebug.print("Waitin" + time / 1000 + " seconds", TAG);

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LoggerDebug.print(time / 1000 + " seconds Ready trying again!", TAG);
    }


    /**
     * Parse Message Object
     *
     * @param username Username
     * @param message  MessageContent
     * @param user     Remitent, is from ava o from the user
     */

    private void addMessage(String username, String message, boolean user) {
        this.saveMessage(new ChatMessage(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()), user, message, username, "date", "image_url", "static"));
    }

    /**
     * Get ChatMessage and save it in the database. If it is an Ava response, notify to Observers
     *
     * @param chatMessage
     */

    private void saveMessage(ChatMessage chatMessage) {
        executor.execute(() -> {
            messageDao.save(chatMessage);
            LoggerDebug.print("Message Saved!: " + chatMessage.getMessage(), TAG);

        });
        if (!chatMessage.getUser()) { //if its Ava's response, notify the observers!
            LoggerDebug.printMessageTrace("Is Ava Response, message: " + chatMessage.getMessage(), TAG, LoggerDebug.MODE_RECEIVING);
            this.notifyNewMessagesObserver(chatMessage);
        }


    }

    /**
     * Sending Message from Class<? ViewModel> to this repository, the message be saved and sended
     *
     * @param messageOutput String that contains message
     */

    public void sendMessage(ChatMessage messageOutput) {
        LoggerDebug.print("trying to send a Message!", TAG);
        if (null == mUsername) return;
        if (userID == null) getUserID();
        if (isListening) {
            LoggerDebug.print("Apollo calls count: " + apolloClient.activeCallsCount()
                    , TAG);
            LoggerDebug.print("Araig boyz im listening!", TAG);
            saveMessage(messageOutput);
            apolloClient.mutate(MutationMessageMutation.builder().message(MessageInput.builder().userId(userID).text(messageOutput.getMessage()).build()).build()).enqueue(new Callback<Data>() {
                @Override
                public void onResponse(@NotNull Response<Data> response) {
                    if (response.data() != null) {
                        LoggerDebug.print(TAG, "response from mutation: " + response.data().toString());
                        LoggerDebug.print("Success MODERFUCKER", TAG);

                    } else
                        LoggerDebug.print(TAG, "Response is null!!");
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LoggerDebug.print(TAG, "Failure Error: " + e.getCause() + " 2_ " + e.getMessage() + "3." + e.getLocalizedMessage());
                    //  retrySendMessage(messageOutput);

                }

                @Override
                public void onNetworkError(@NotNull ApolloNetworkException e) {
                    super.onNetworkError(e);
                    LoggerDebug.print(TAG, "Netwok Error: " + e.getCause() + " 2_ " + e.getMessage() + "3." + e.getLocalizedMessage());
                    executor.execute(()->{
                        waitsFunction(longRetryTime);
                        sendMessage(messageOutput);
                    });

                }

                @Override
                public void onHttpError(@NotNull ApolloHttpException e) {
                    LoggerDebug.print(TAG, "Htttp Error: " + e.getCause() + " 2_ " + e.getMessage() + "3." + e.getLocalizedMessage());

                }

            });
            LoggerDebug.printMessageTrace(messageOutput.getMessage(), TAG, LoggerDebug.MODE_SENDING);
        } else {
            executor.execute(() -> {
                startListening();
                waitsFunction(shortRetryTime);
                if (!isListening) {
                    waitsFunction(longRetryTime);
                    LoggerDebug.print("Isnt listen", TAG);
                } else {
                    LoggerDebug.print("is Listening!", TAG);
                }
                sendMessage(messageOutput);


            });
        }



    }

    public void getInitialData() {
        executor.execute(() ->
                this.notifyInitialMessages(messageDao.loadAllMessagesWithExecutor()));
    }

    public List<ChatMessage> getInitialMessages() {
        return initialMessages;
    }

    private void queryTest() {
        apolloClient.query(AboutUsQuery.builder().build()).enqueue(new Callback<AboutUsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<AboutUsQuery.Data> response) {
                if (response.data() != null)
                    LoggerDebug.print(TAG, "Response from Query: " + response.data().toString());
                else
                    LoggerDebug.print(TAG, "Response is null!!");
            }
            @Override
            public void onFailure(@NotNull ApolloException e) {
                LoggerDebug.print(TAG, "Error: " + e.getCause() + " 2_ " + e.getMessage() + "3." + e.getLocalizedMessage());


            }
        });
    }

    private String extractMessage(@NotNull String message) {
        final String initialDelimiter = "text=";
        Log.d(TAG, "Extract Message: " + message.substring(message.indexOf("text=") + initialDelimiter.length(), message.indexOf("}}")));
        return message.substring(message.indexOf("text=") + initialDelimiter.length(), message.indexOf("}}"));
    }

    private boolean userIdExists() {
        return userID != null;
    }

    public void closeConnection() {
        LoggerDebug.print("Trying to close connection", TAG);
        apolloClient.mutate(DeleteThisUserMutation.builder().deleteUser(DeleteUserInput.builder().userId(userID).username(mUsername).build()).build())
                .enqueue(new Callback<DeleteThisUserMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<DeleteThisUserMutation.Data> response) {
                        if (response.data() != null)
                            LoggerDebug.print("User deleted!" + response.data().deleteUser(), TAG);

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        LoggerDebug.print("Failed At deleting user!", TAG);

                    }
                });

    }
}
