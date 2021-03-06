package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.LocationType;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Location;
import ch.uzh.ifi.seal.soprafs20.entity.Message;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.*;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserGetDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPutDTO;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST request without actually sending them over the network.
 * This tests if the UserController works.
 */


@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Code 201 post /users
    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setUsername("testUsername");
        user.setPassword("password");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setName("Test User");
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("password");

        given(userService.createUser(Mockito.any())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        MvcResult response = mockMvc.perform(postRequest).andReturn();
        String responseAsString = response.getResponse().getContentAsString();

        Assertions.assertEquals(user.getName(), JsonPath.parse(responseAsString).read("$.name"));
        Assertions.assertEquals(user.getPassword(), JsonPath.parse(responseAsString).read("$.password"));
        Assertions.assertEquals(user.getUsername(), JsonPath.parse(responseAsString).read("$.username"));
        Assertions.assertEquals(user.getStatus().toString(), JsonPath.parse(responseAsString).read("$.status"));
        Assertions.assertEquals(201, response.getResponse().getStatus());
    }

    // Code 409 post /users
    @Test
    public void createUser_userAlreadyExists() throws Exception {
        User user2 = new User();

        user2.setId(2);
        user2.setName("Test User2");
        user2.setUsername("testUsername");
        user2.setPassword("password");
        user2.setToken("2");
        user2.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO2 = new UserPostDTO();
        userPostDTO2.setName("Test User2");
        userPostDTO2.setUsername("testUsername");
        userPostDTO2.setPassword("password");

        given(userService.createUser(Mockito.any())).willThrow(new DuplicatedUserException("The username provided is not unique. Therefore, the user could not be created!"));

        MockHttpServletRequestBuilder postRequest2 = post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO2));

        MvcResult response = mockMvc.perform(postRequest2).andReturn();

        Assertions.assertEquals(409, response.getResponse().getStatus());
        Assertions.assertEquals("The username provided is not unique. Therefore, the user could not be created!", response.getResolvedException().getMessage());
    }

    // Code 200 put /login
    @Test
    public void loginUser_validInput() throws Exception {
        User user = new User();
        user.setId(1);
        user.setUsername("testUsername");
        user.setPassword("password");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setPassword("password");

        given(userService.checkForLogin(Mockito.any())).willReturn(user);

        MockHttpServletRequestBuilder putRequest = put("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        MvcResult response = mockMvc.perform(putRequest).andReturn();
        String responseAsString = response.getResponse().getContentAsString();

        Assertions.assertEquals(user.getPassword(), JsonPath.parse(responseAsString).read("$.password"));
        Assertions.assertEquals(user.getUsername(), JsonPath.parse(responseAsString).read("$.username"));
        Assertions.assertEquals(user.getStatus().toString(), JsonPath.parse(responseAsString).read("$.status"));
        Assertions.assertEquals(200, response.getResponse().getStatus());
    }

    // Code 401 put /login
    @Test
    public void loginUser_invalidCredentials() throws Exception{
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setPassword("password");

        given(userService.checkForLogin(Mockito.any())).willThrow(new InvalidCredentialsException("Wrong password, please try again."));

        MockHttpServletRequestBuilder putRequest = put("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(401, response.getResponse().getStatus());
        Assertions.assertEquals("Wrong password, please try again.", response.getResolvedException().getMessage());

    }

    // Code 200 get /users/{userId}
    @Test
    public void getUser_validInput() throws Exception {
        // given
        User user = new User();
        user.setName("Firstname Lastname");
        user.setUsername("firstname@lastname");
        user.setPassword("password");
        user.setCreationDate("2020-03-04 13:54:14.474");
        user.setBirthDate("05.07.1999");
        user.setStatus(UserStatus.OFFLINE);
        user.setId(1);

        given(userService.getUserById(1)).willReturn(user);

        MockHttpServletRequestBuilder getRequest = get("/users/1").contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();
        String responseAsString = response.getResponse().getContentAsString();

        Assertions.assertEquals(user.getName(), JsonPath.parse(responseAsString).read("$.name"));
        Assertions.assertEquals(user.getPassword(), JsonPath.parse(responseAsString).read("$.password"));
        Assertions.assertEquals(user.getUsername(), JsonPath.parse(responseAsString).read("$.username"));
        Assertions.assertEquals(user.getStatus().toString(), JsonPath.parse(responseAsString).read("$.status"));
        Assertions.assertEquals(user.getCreationDate(), JsonPath.parse(responseAsString).read("$.creationDate"));
        Assertions.assertEquals(200, response.getResponse().getStatus());
    }

    // Code 404 get /users/{userId}
    @Test
    public void getUser_userDoesNotExist() throws Exception {
        given(userService.getUserById(Mockito.anyInt())).willThrow(new UserNotFoundException("This user could not be found."));

        MockHttpServletRequestBuilder getRequest = get("/users/99")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();

        Assertions.assertEquals(404, response.getResponse().getStatus());
        Assertions.assertEquals("This user could not be found.", response.getResolvedException().getMessage());
    }

    // Code 204 put /users/{userId}
    @Test
    public void updateUser_userExists() throws Exception{
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("username");
        userPutDTO.setBirthDate("01.01.1999");

        doNothing().when(userService).updateUser(1, userPutDTO);

        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(204, response.getResponse().getStatus());
        Assertions.assertEquals("", response.getResponse().getContentAsString());
    }

    // Code 404 put /users/{userId}
    @Test
    public void updateUser_userDoesNotExist() throws Exception {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("username");
        userPutDTO.setBirthDate("01.01.1999");

        doThrow(new UserNotFoundException("This user could not be found.")).when(userService).updateUser(Mockito.anyInt(), Mockito.any(UserPutDTO.class));


        MockHttpServletRequestBuilder putRequest = put("/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(404, response.getResponse().getStatus());
        Assertions.assertEquals("This user could not be found.", response.getResolvedException().getMessage());
    }

    // Code 200 get /users/friends/{userId}
    @Test
    public void getAllFriends_validInput() throws Exception {
        // given
        User user = new User();
        user.setName("Firstname Lastname");
        user.setUsername("firstname@lastname");
        user.setPassword("password");
        user.setCreationDate("2020-03-04 13:54:14.474");
        user.setBirthDate("05.07.1999");
        user.setStatus(UserStatus.OFFLINE);
        user.setId(1);

        ArrayList<User> allFriends = new ArrayList<>();
        allFriends.add(user);

        given(userService.getFriends(5)).willReturn(allFriends);

        MockHttpServletRequestBuilder getRequest = get("/users/friends/5").contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();
        String responseAsString = response.getResponse().getContentAsString();

        Assertions.assertEquals(user.getName(), JsonPath.parse(responseAsString).read("$.[0].name"));
        Assertions.assertEquals(user.getPassword(), JsonPath.parse(responseAsString).read("$.[0].password"));
        Assertions.assertEquals(user.getUsername(), JsonPath.parse(responseAsString).read("$.[0].username"));
        Assertions.assertEquals(user.getStatus().toString(), JsonPath.parse(responseAsString).read("$.[0].status"));
        Assertions.assertEquals(user.getCreationDate(), JsonPath.parse(responseAsString).read("$.[0].creationDate"));
        Assertions.assertEquals(200, response.getResponse().getStatus());
    }

    // Code 204 put /users/friends/{userId}/{friendId}
    @Test
    public void addFriend_validInput() throws Exception{
        doNothing().when(userService).addFriend(1, 5);

        MockHttpServletRequestBuilder putRequest = put("/users/friends/1/5")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(204, response.getResponse().getStatus());
        Assertions.assertEquals("", response.getResponse().getContentAsString());
    }

    // Code 204 delete /users/friends/{userId}/{friendId}
    @Test
    public void deleteFriend_validInput() throws Exception{
        doNothing().when(userService).deleteFriend(1, 5);

        MockHttpServletRequestBuilder putRequest = delete("/users/friends/1/5")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(204, response.getResponse().getStatus());
        Assertions.assertEquals("", response.getResponse().getContentAsString());
    }

    // Code 200 get /users/friends/{userId}/{friendId}
    @Test
    public void checkFriend_validInput() throws Exception{
        given(userService.checkFriend(Mockito.anyInt(), Mockito.anyInt())).willReturn(true);

        MockHttpServletRequestBuilder getRequest = get("/users/friends/1/5")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();

        Assertions.assertEquals(200, response.getResponse().getStatus());
        Assertions.assertEquals("true", response.getResponse().getContentAsString());
    }

    // Code 200 get /users/friends/{userId}/{friendId}
    @Test
    public void checkFriend_invalidInput() throws Exception{
        doThrow(new UserNotFoundException("This user could not be found.")).when(userService).checkFriend(Mockito.anyInt(), Mockito.anyInt());


        MockHttpServletRequestBuilder getRequest = get("/users/friends/1/5")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();

        Assertions.assertEquals(404, response.getResponse().getStatus());
        Assertions.assertEquals("This user could not be found.", response.getResolvedException().getMessage());
    }

    // Code 200 get /users
    @Test
    public void getAllUsers_validInput() throws Exception {
        // given
        User user = new User();
        user.setName("Firstname Lastname");
        user.setUsername("firstname@lastname");
        user.setPassword("password");
        user.setCreationDate("2020-03-04 13:54:14.474");
        user.setBirthDate("05.07.1999");
        user.setStatus(UserStatus.OFFLINE);
        user.setId(1);

        ArrayList<User> allUsers = new ArrayList<>();
        allUsers.add(user);

        given(userService.getAllUsers()).willReturn(allUsers);

        MockHttpServletRequestBuilder getRequest = get("/users/").contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();
        String responseAsString = response.getResponse().getContentAsString();

        Assertions.assertEquals(user.getName(), JsonPath.parse(responseAsString).read("$.[0].name"));
        Assertions.assertEquals(user.getPassword(), JsonPath.parse(responseAsString).read("$.[0].password"));
        Assertions.assertEquals(user.getUsername(), JsonPath.parse(responseAsString).read("$.[0].username"));
        Assertions.assertEquals(user.getStatus().toString(), JsonPath.parse(responseAsString).read("$.[0].status"));
        Assertions.assertEquals(user.getCreationDate(), JsonPath.parse(responseAsString).read("$.[0].creationDate"));
        Assertions.assertEquals(200, response.getResponse().getStatus());
    }

    // Code 200 get /users/chats/{userId}/{friendId}
    @Test
    public void getFriendsChat_validInput() throws Exception {
        // given
        Message message = new Message();
        message.setContent("Hello");
        message.setSenderId(1);
        message.setSenderUsername("Username");
        message.setTimestamp("29.04, 19:55");
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);

        given(userService.getChat(1, 2)).willReturn(messages);

        MockHttpServletRequestBuilder getRequest = get("/users/chats/1/2").contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();
        String responseAsString = response.getResponse().getContentAsString();

        Assertions.assertEquals(messages.get(0).getContent(), JsonPath.parse(responseAsString).read("$.[0].content"));
        Assertions.assertEquals(messages.get(0).getSenderUsername(), JsonPath.parse(responseAsString).read("$.[0].senderUsername"));
        Assertions.assertEquals(messages.get(0).getTimestamp(), JsonPath.parse(responseAsString).read("$.[0].timestamp"));
        Assertions.assertEquals(200, response.getResponse().getStatus());
    }

    // Code 200 get /users/chats/{userId}/{friendId}
    @Test
    public void getFriendsChat_invalidInput() throws Exception{
        given(userService.getChat(Mockito.anyInt(), Mockito.anyInt())).willThrow(new UserNotFoundException("This user-friend pair could not be found"));

        MockHttpServletRequestBuilder getRequest = get("/users/chats/1/2")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();

        Assertions.assertEquals(404, response.getResponse().getStatus());
        Assertions.assertEquals("This user-friend pair could not be found", response.getResolvedException().getMessage());
    }

    // Code 204 put /users/chats/{userId}/{friendId}
    @Test
    public void postMessageFriend_validInput() throws Exception{
        Message message = new Message();
        message.setContent("Hello");
        message.setSenderId(1);
        message.setTimestamp("29.04, 19:55");

        doNothing().when(userService).postMessage(1, 2, message);

        MockHttpServletRequestBuilder putRequest = put("/users/chats/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(message));

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(204, response.getResponse().getStatus());
        Assertions.assertEquals("", response.getResponse().getContentAsString());

    }

    // Code 204 put /users/chats/{userId}/{friendId}
    @Test
    public void postMessageFriend_invalidInput() throws Exception{
        Message message = new Message();
        message.setContent("Hello");
        message.setSenderId(1);
        message.setTimestamp("19:55");

        doThrow(new UserNotFoundException("This user-friend pair could not be found")).when(userService).postMessage(Mockito.anyInt(), Mockito.anyInt(), Mockito.any());

        MockHttpServletRequestBuilder putRequest = put("/users/chats/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(message));

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(404, response.getResponse().getStatus());
        Assertions.assertEquals("This user-friend pair could not be found", response.getResolvedException().getMessage());
    }

    // Code 204 delete /locations/chats/{locationId}
    @Test
    public void deleteMessageFriend_validInput() throws Exception{
        doNothing().when(userService).deleteMessage(1, 2,12345);

        MockHttpServletRequestBuilder putRequest = delete("/users/chats/1/2/12345")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(204, response.getResponse().getStatus());
        Assertions.assertEquals("", response.getResponse().getContentAsString());

    }

    // Code 404 delete /locations/chats/{locationId}
    @Test
    public void deleteMessageFriend_invalidInput() throws Exception{
        doThrow(new UserNotFoundException("This user-friend pair could not be found")).when(userService).deleteMessage(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());

        MockHttpServletRequestBuilder putRequest = delete("/users/chats/1/2/12345")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(putRequest).andReturn();

        Assertions.assertEquals(404, response.getResponse().getStatus());
        Assertions.assertEquals("This user-friend pair could not be found", response.getResolvedException().getMessage());
    }

    // Code 200 get /users/chats/news/{userId}/{friendId}
    @Test
    public void checkUnreadMessage_validInput() throws Exception{
        given(userService.checkUnreadMessages(Mockito.anyInt(), Mockito.anyInt())).willReturn(true);

        MockHttpServletRequestBuilder getRequest = get("/users/chats/news/1/5")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();

        Assertions.assertEquals(200, response.getResponse().getStatus());
        Assertions.assertEquals("true", response.getResponse().getContentAsString());
    }

    // Code 404 get /users/chats/news/{userId}/{friendId}
    @Test
    public void checkUnreadMessage_invalidInput() throws Exception{
        doThrow(new UserNotFoundException("This user-friend pair could not be found")).when(userService).checkUnreadMessages(Mockito.anyInt(), Mockito.anyInt());


        MockHttpServletRequestBuilder getRequest = get("/users/chats/news/1/5")
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult response = mockMvc.perform(getRequest).andReturn();

        Assertions.assertEquals(404, response.getResponse().getStatus());
        Assertions.assertEquals("This user-friend pair could not be found", response.getResolvedException().getMessage());
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     * @param object
     * @return string
     */

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new SopraServiceException(String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
