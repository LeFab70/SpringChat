package org.fab.chat.services;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveUserServiceImpl implements ActiveUserService {

    private final Set<String> activeUsernames = ConcurrentHashMap.newKeySet();

    @Override
    public boolean tryAdd(String username) {
        return activeUsernames.add(username);
    }

    @Override
    public void remove(String username) {
        activeUsernames.remove(username);
    }

}
