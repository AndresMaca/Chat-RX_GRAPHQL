package com.coders.dope.repositories;

public interface ApolloConnection {
    void startListening();
    void closeConnection();
    void connectWithServer();
}
