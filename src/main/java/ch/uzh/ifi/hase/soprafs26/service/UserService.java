package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.Date;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public User createUser(User newUser) {
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.ONLINE); // changed to online, user is automatically logged in after registration
		newUser.setCreationDate(new Date());
		newUser.setName(newUser.getUsername()); // set name to username to satisfy not-null constraint
		checkIfUserExists(newUser);
		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	// add login method
	public User loginUser(User loginUser) {
		User userByUsername = userRepository.findByUsername(loginUser.getUsername());

		// check if user exists
		if (userByUsername == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
		}

		// check if password matches
		if (!userByUsername.getPassword().equals(loginUser.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password!");
		}

		// set status to ONLINE
		userByUsername.setStatus(UserStatus.ONLINE);
		userRepository.flush();

		return userByUsername;
	}

	// add get user by id method
	public User getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
	}

	// add update user method
	public User updateUser(Long userId, User updatedUser) {
		User user = getUserById(userId);
		
		// update password if provided
		if (updatedUser.getPassword() != null) {
			user.setPassword(updatedUser.getPassword());
		}

		// update bio if provided
		if (updatedUser.getBio() != null) {
			user.setBio(updatedUser.getBio());
		}

		userRepository.flush();
		return user;
	}

	// helper method to find a user by their token for authentication
	public User getUserByToken(String token) {
		return userRepository.findByToken(token);
	}




	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */

	// checks if username is already taken, throws 409 conflict error if taken
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
		if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken!");
		}
	}
}
