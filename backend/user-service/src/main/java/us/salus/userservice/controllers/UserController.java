package us.salus.userservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import us.salus.userservice.models.User;
import us.salus.userservice.services.JWTService;
import us.salus.userservice.services.UserService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JWTService jwtService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
        this.jwtService = new JWTService();
    }

    // Basic CRUD endpoints
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{name}")
    public ResponseEntity<User> getUserByName(@PathVariable String name) {
        Optional<User> user = userService.getUserByName(name);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{name}")
    public ResponseEntity<User> updateUser(@PathVariable String name, @RequestBody User user) {
        Optional<User> updatedUser = userService.updateUser(name, user);
        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteUser(@PathVariable String name) {
        boolean deleted = userService.deleteUser(name);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Conversation history endpoints
    @GetMapping("/{name}/conversations")
    public ResponseEntity<List<User.ConversationEntry>> getConversationHistory(@PathVariable String name) {
        List<User.ConversationEntry> conversations = userService.getConversationHistory(name);
        return conversations != null ? ResponseEntity.ok(conversations) : ResponseEntity.notFound().build();
    }

    @GetMapping("/id/{id}/conversations")
    public ResponseEntity<List<User.ConversationEntry>> getConversationHistory(@PathVariable long id) {
        List<User.ConversationEntry> conversations = userService.getConversationHistoryById(id);
        return conversations != null ? ResponseEntity.ok(conversations) : ResponseEntity.notFound().build();
    }

    @GetMapping("/id/{id}/conversations/range")
    public ResponseEntity<List<User.ConversationEntry>> getConversationHistoryBetweenDates(
            @PathVariable long id,
            @RequestParam ZonedDateTime startDate,
            @RequestParam ZonedDateTime endDate) {
        List<User.ConversationEntry> conversations = userService.getConversationHistoryBetweenDatesById(id, startDate,
                endDate);
        return conversations != null ? ResponseEntity.ok(conversations) : ResponseEntity.notFound().build();
    }

    @PostMapping("/id/{id}/conversations")
    public ResponseEntity<User.ConversationEntry> addConversationEntry(
            @PathVariable long id,
            @RequestBody User.ConversationEntry conversationEntry) {
        User.ConversationEntry addedEntry = userService.addConversationEntryById(id, conversationEntry);
        return addedEntry != null ? ResponseEntity.status(HttpStatus.CREATED).body(addedEntry)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{name}/conversations/range")
    public ResponseEntity<List<User.ConversationEntry>> getConversationHistoryBetweenDates(
            @PathVariable String name,
            @RequestParam ZonedDateTime startDate,
            @RequestParam ZonedDateTime endDate) {
        List<User.ConversationEntry> conversations = userService.getConversationHistoryBetweenDates(name, startDate,
                endDate);
        return conversations != null ? ResponseEntity.ok(conversations) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{name}/conversations")
    public ResponseEntity<User.ConversationEntry> addConversationEntry(
            @PathVariable String name,
            @RequestBody User.ConversationEntry conversationEntry) {
        User.ConversationEntry addedEntry = userService.addConversationEntry(name, conversationEntry);
        return addedEntry != null ? ResponseEntity.status(HttpStatus.CREATED).body(addedEntry)
                : ResponseEntity.notFound().build();
    }

    // Activity endpoints
    @PostMapping("/{name}/activities")
    public ResponseEntity<User> addHistoricalActivity(
            @PathVariable String name,
            @RequestBody User.HistoricalActivity activity) {
        User updatedUser = userService.addHistoricalActivity(name, activity);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    // Calorie endpoints
    @PostMapping("/{name}/calories")
    public ResponseEntity<User> addHistoricalCalories(
            @PathVariable String name,
            @RequestBody User.HistoricalCalories calories) {
        User updatedUser = userService.addHistoricalCalories(name, calories);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    // Meal endpoints
    @PostMapping("/{name}/meals")
    public ResponseEntity<User> addHistoricalMeal(
            @PathVariable String name,
            @RequestBody User.HistoricalMeal meal) {
        User updatedUser = userService.addHistoricalMeal(name, meal);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @PostMapping("/id/{id}/activities")
    public ResponseEntity<User> addHistoricalActivity(
            @PathVariable long id,
            @RequestBody User.HistoricalActivity activity) {
        User updatedUser = userService.addHistoricalActivityById(id, activity);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @PostMapping("/id/{id}/calories")
    public ResponseEntity<User> addHistoricalCalories(
            @PathVariable long id,
            @RequestBody User.HistoricalCalories calories) {
        User updatedUser = userService.addHistoricalCaloriesById(id, calories);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @PostMapping("/id/{id}/meals")
    public ResponseEntity<User> addHistoricalMeal(
            @PathVariable long id,
            @RequestBody User.HistoricalMeal meal) {
        User updatedUser = userService.addHistoricalMealById(id, meal);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(@CookieValue(value = "salus_session") String jwtCookie) {
        DecodedJWT jwt;
        try {
            jwt = jwtService.verifyJWT(jwtCookie);
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwt.getClaim("user_id").asLong();

        Optional<User> oUser = userService.getUserById(userId);
        if (oUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(oUser.get());
    }
}
