package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers() {
		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

	// endpoint is called when a user tries to log in. It receives a username and password,
	// 		calls loginUser in the service, and returns the user data.
	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
		// convert DTO to entity
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		
		// call login method in service
		User loggedInUser = userService.loginUser(userInput);
		
		// convert result back to DTO and return
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
	}

	// endpoint is called when someone wants to view a specific user's profile.
	// 		takes userId from URL, validates token, finds user and returns their data.
	// 		returns 200 if found, 404 if not found, 401 if not authenticated.
	@GetMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO getUserById(@PathVariable Long userId,
        @RequestHeader(value = "Authorization", required = false) String token) {
    
		// check if token is provided and valid
		if (token == null || userService.getUserByToken(token) == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated!");
		}

		// fetch user by id
		User user = userService.getUserById(userId);

		// convert and return
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}

	// endpoint is called when a user wants to update their password.
	// 		takes userId from URL, validates token, updates user data.
	// 		returns 204 if successful, 404 if user not found, 401 if not authenticated.
	@PutMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateUser(@PathVariable Long userId,
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody UserPostDTO userPostDTO) {

		// check if token is provided and valid
		if (token == null || userService.getUserByToken(token) == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated!");
		}

		// convert DTO to entity
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// update user
		userService.updateUser(userId, userInput);
	}

}
