package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;

@RestController
@RequestMapping("profile")
@CrossOrigin
public class ProfileController
{
    private ProfileDao profileDao;
    private UserDao userDao;

    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao)
    {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Profile getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            int userId = userDao.getIdByUsername(username);

            if(userId == -1) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            Profile profile = profileDao.getByUserId(userId);

            if(profile == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");

            return profile;
        } catch(Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", ex);
        }
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public void updateProfile(Authentication authentication, @RequestBody Profile profile)
    {
        try
        {
            String username = authentication.getName();
            int userId = userDao.getIdByUsername(username);

            // Profile.userId is ignored. the authenticated logged in userId is used instead.
            profileDao.update(userId, profile);
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", ex);
        }
    }
}
