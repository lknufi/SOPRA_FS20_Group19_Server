package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.database.DatabaseConnector;
import ch.uzh.ifi.seal.soprafs20.database.DatabaseConnectorUser;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.DuplicatedUserException;
import ch.uzh.ifi.seal.soprafs20.exceptions.InvalidCredentialsException;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UserNotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPutDTO;
import org.hibernate.dialect.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.crypto.Data;
import java.sql.Timestamp;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);


    // returns a user in the repository identified by his ID
    public User getUserById(Long id){
        //Not yet implemented
        return new User();
    }

    // updates a user with the updated username and birthdate
    public void updateUser(Long userId, UserPutDTO userWithNewData) {
        //not yet implemented
    }

    // logs out all users
    public void logoutUsers(){
        //not yet implemented
    }

    // checks if the user that is attempting a login has the correct credentials
    public User checkForLogin(User userToBeLoggedIn){

        boolean validCredentials = DatabaseConnectorUser.checkIfUserAndPasswordExist(userToBeLoggedIn.getUsername(), userToBeLoggedIn.getPassword());

        //Check if user is allowed to log in
        if (!validCredentials){ //case that the credentials are invalid
            boolean validUsername = DatabaseConnectorUser.checkIfUsernameExists(userToBeLoggedIn.getUsername());
            //check if the username is valid, if yes, the password must be false
            String message = validUsername ? "Wrong password, please try again." : "This username does not exist, please register first.";
            throw new InvalidCredentialsException(message);
        }

        //If credentials are valid set the user status to online, since the user will be logged in
        DatabaseConnectorUser.setOnlineStatus(userToBeLoggedIn, true);
        //return new user representation
        return DatabaseConnectorUser.getUserByUsername(userToBeLoggedIn.getUsername());
    }

    // creates a new user in the user repository
    public User createUser(User newUser) {
        newUser.setCreationDate(UserService.getCurrentDate());
        //Checks whether username is already taken
        if (DatabaseConnectorUser.checkIfUsernameExists(newUser.getUsername())){
            throw new DuplicatedUserException("The provided username is already taken. Please try a new one.");
        }
        //If the username is not already taken create a new User in the database
        DatabaseConnectorUser.createUser(newUser);

        log.debug("Created Information for User: {}", newUser);

        return newUser;
    }

    // creates a timestamp of the current date for the creation date during the registration
    public static String getCurrentDate(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return timestamp.toString();
    }
}
