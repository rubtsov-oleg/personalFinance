package ru.skillFactory.service;

import ru.skillFactory.model.User;

import java.util.HashMap;

public interface AuthService {
    String INCOMING_TRANSFERS_NAME = "Входящие переводы";
    String OUTGOING_TRANSFERS_NAME = "Исходящие переводы";

    void signup();

    User signIn();

    void saveUsersToFile();

    void loadUsersFromFile();

    HashMap<String, User> getUsers();
}
