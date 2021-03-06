package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.database.DatabaseConnectorLocation;
import ch.uzh.ifi.seal.soprafs20.database.DatabaseConnectorLocationChats;
import ch.uzh.ifi.seal.soprafs20.database.DatabaseConnectorUser;
import ch.uzh.ifi.seal.soprafs20.entity.Message;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.DuplicatedUserException;
import ch.uzh.ifi.seal.soprafs20.exceptions.InvalidCredentialsException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UserNotFoundException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    //returns all users in the database
    public ArrayList<User> getAllUsers(){
        return DatabaseConnectorUser.getAllUsers();
    }

    // returns a user in the repository identified by his ID
    public User getUserById(int id){
        User userToReturn = DatabaseConnectorUser.getUserById(id);
        return  userToReturn;
    }

    // updates a user with the updated name, username and password
    public void updateUser(int userId, UserPutDTO userWithNewData) {
        User userToUpdate = DatabaseConnectorUser.getUserById(userId);

        if (userToUpdate == null){
            throw new UserNotFoundException("This user could not be found.");
        }

        if (userWithNewData.getName() != null){
            userToUpdate.setName(userWithNewData.getName());
            DatabaseConnectorUser.updateName(userToUpdate);
        }

        if (userWithNewData.getUsername() != null){
            if (DatabaseConnectorUser.checkIfUsernameExists(userWithNewData.getUsername())){
                throw new DuplicatedUserException("The provided username is already taken. Please try a new one.");
            }
            userToUpdate.setUsername(userWithNewData.getUsername());
            DatabaseConnectorUser.updateUsername(userToUpdate);
        }

        if (userWithNewData.getPassword() != null){
            userToUpdate.setPassword(userWithNewData.getPassword());
            DatabaseConnectorUser.updatePassword(userToUpdate);
        }

        if (userWithNewData.getAvatarNr() != 0){
            userToUpdate.setAvatarNr(userWithNewData.getAvatarNr());
            DatabaseConnectorUser.updateAvatarNr(userToUpdate);
        }

    }

    // logs out the user
    public void logoutUser(int id){
        DatabaseConnectorUser.setOnlineStatusById(id, false);
    }

    // checks if the user that is attempting a login has the correct credentials
    public User checkForLogin(User userToBeLoggedIn) {

        boolean validCredentials = DatabaseConnectorUser.checkIfUserAndPasswordExist(userToBeLoggedIn.getUsername(), userToBeLoggedIn.getPassword());

        //Check if user is allowed to log in
        if (!validCredentials) { //case that the credentials are invalid
            boolean validUsername = DatabaseConnectorUser.checkIfUsernameExists(userToBeLoggedIn.getUsername());
            //check if the username is valid, if yes, the password must be false
            String message = validUsername ? "Wrong password, please try again." : "This username does not exist, please register first.";
            throw new InvalidCredentialsException(message);
        }

        //If credentials are valid set the user status to online, since the user will be logged in
        DatabaseConnectorUser.setOnlineStatus(userToBeLoggedIn, true);
        //return new user representation
        User user = DatabaseConnectorUser.getUserByUsername(userToBeLoggedIn.getUsername());

        return user;
    }

    // creates a new user in the user repository
    public User createUser(User newUser) {
        newUser.setCreationDate(UserService.getCurrentDate());
        newUser.setFavoriteLocations(new ArrayList<Integer>());
        newUser.setFriendsList(new ArrayList<Integer>());
        //Checks whether username is already taken
        if (DatabaseConnectorUser.checkIfUsernameExists(newUser.getUsername())){
            throw new DuplicatedUserException("The provided username is already taken. Please try a new one.");
        }
        //If the username is not already taken create a new User in the database
        User userToReturn = DatabaseConnectorUser.createUser(newUser);

        log.debug("Created Information for User: {}", newUser);

        return userToReturn;
    }

    public void deleteFriend(int deletingUserId, int toBeDeletedUserId){
        DatabaseConnectorUser.deleteFriend(deletingUserId, toBeDeletedUserId);
    }

    public void addFriend(int addingUserId, int toBeAddedUserId){
        // check if user exists
        DatabaseConnectorUser.getUserById(toBeAddedUserId);
        // add user to friends list if userIds are not the same
        if (addingUserId != toBeAddedUserId){
            DatabaseConnectorUser.addFriend(addingUserId, toBeAddedUserId);
        }
        // create a new chat for the friends pair
        if (addingUserId <= toBeAddedUserId && addingUserId != toBeAddedUserId){
            if (!DatabaseConnectorLocationChats.checkForExistingChat(addingUserId, toBeAddedUserId)){
                DatabaseConnectorLocationChats.createNewFriendsChat(addingUserId, toBeAddedUserId);
            }
        }
        else if (addingUserId != toBeAddedUserId){
            if (!DatabaseConnectorLocationChats.checkForExistingChat(toBeAddedUserId, addingUserId)){
                DatabaseConnectorLocationChats.createNewFriendsChat(toBeAddedUserId, addingUserId);
            }
        }
    }

    public boolean checkFriend(int userId, int friendId){
        ArrayList<Integer> friendsIds = DatabaseConnectorUser.getFriends(userId);
        if (friendsIds.contains(friendId)){
            return true;
        }
        else {
            return false;
        }
    }

    public ArrayList<User> getFriends(int userId){
        ArrayList<Integer> friendIds = DatabaseConnectorUser.getFriends(userId);
        ArrayList<User> friendsAsUsers = new ArrayList<>();

        for (Integer friendId : friendIds){
            friendsAsUsers.add(getUserById(friendId));
        }

        return friendsAsUsers;
    }

    public ArrayList<Message> getChat(int userId, int friendId){
        ArrayList<Message> chatToReturn;
        if (userId <= friendId){
            chatToReturn = DatabaseConnectorLocationChats.getChatFriends(userId, friendId);
            DatabaseConnectorLocationChats.setOnRead(userId, friendId, userId);
        }
        else {
            chatToReturn = DatabaseConnectorLocationChats.getChatFriends(friendId, userId);
            DatabaseConnectorLocationChats.setOnRead(friendId, userId, userId);
        }

        return chatToReturn;
    }

    public boolean checkUnreadMessages(int userId, int friendId){
        boolean isUnread;
        if (userId <= friendId){
            isUnread = DatabaseConnectorLocationChats.checkUnreadMessages(userId, friendId, userId);
        }
        else {
            isUnread = DatabaseConnectorLocationChats.checkUnreadMessages(friendId, userId, userId);
        }
        return isUnread;
    }

    public void postMessage(int userId, int friendId, Message message){
        message.setTimestamp(getCurrentTimestamp());
        DatabaseConnectorLocationChats.postMessageFriends(userId, friendId, message);
    }

    public void deleteMessage(int userId, int friendId, int messageId){
        DatabaseConnectorLocationChats.deleteMessageFriends(userId, friendId, messageId);
    }

    // creates a timestamp of the current date for the creation date during the registration
    public static String getCurrentDate(){

        Date datenow = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LocalDate date = LocalDate.now();
        //return date.toString();
        return dateFormat.format(datenow);
    }

    public String getCurrentTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM, HH:mm");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() + 1000 * 60 * 60 * 2);
        return sdf.format(timestamp);
    }
}
