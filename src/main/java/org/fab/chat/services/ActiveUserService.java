package org.fab.chat.services;

public interface ActiveUserService {

    /**
     * @return true if the username was free and is now reserved, false if it was already taken.
     */
    boolean tryAdd(String username);

    void remove(String username);

}
