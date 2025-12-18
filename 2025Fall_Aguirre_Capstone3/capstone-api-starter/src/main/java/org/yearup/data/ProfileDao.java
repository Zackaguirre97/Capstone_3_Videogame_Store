package org.yearup.data;


import org.yearup.models.Profile;

public interface ProfileDao
{
    // Used by AuthenticationController to create a new Profile
    Profile create(Profile profile);

    // Used by ProfileController to view and update a profile
    Profile getByUserId(int userId);
    void update(int userId, Profile profile);
}
