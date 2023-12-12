/*
 * Copyright (c) 2022 Redlink GmbH.
 */
package io.redlink.more.studymanager.service;

import io.redlink.more.studymanager.model.MoreUser;
import io.redlink.more.studymanager.model.SearchResult;
import io.redlink.more.studymanager.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SearchResult<MoreUser> findUsers(String query, String institution, int offset, int limit) {
        return userRepository.findUser(query, institution, offset, limit);
    }
}
