package us.salus.userservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.salus.userservice.models.User;
import us.salus.userservice.repositories.UserRepository;
import java.util.stream.Collectors;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Basic CRUD operations
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByName(String name) {
        return userRepository.findById(name);
    }

    public Optional<User> getUserById(long id) {
        // Find user by the numeric id field, not the MongoDB document id
        return userRepository.findAll().stream()
                .filter(user -> user.getId() == id)
                .findFirst();
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> updateUser(String name, User userDetails) {
        Optional<User> userOptional = userRepository.findById(name);
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            // Update user fields (preserving the name as it's the document ID)
            existingUser.setId(userDetails.getId());
            existingUser.setAge(userDetails.getAge());
            existingUser.setWeight(userDetails.getWeight());
            existingUser.setHeight(userDetails.getHeight());
            existingUser.setFitnessGoal(userDetails.getFitnessGoal());
            existingUser.setBmr(userDetails.getBmr());

            // Save updated user
            return Optional.of(userRepository.save(existingUser));
        }
        return Optional.empty();
    }

    public boolean deleteUser(String name) {
        Optional<User> userOptional = userRepository.findById(name);
        if (userOptional.isPresent()) {
            userRepository.deleteById(name);
            return true;
        }
        return false;
    }

    // Conversation-related operations
    public List<User.ConversationEntry> getConversationHistory(String name) {
        Optional<User> userOptional = userRepository.findById(name);
        return userOptional.map(User::getConversationHistory).orElse(null);
    }

    public List<User.ConversationEntry> getConversationHistoryBetweenDates(String name, ZonedDateTime startDate, ZonedDateTime endDate) {
        Optional<User> userOptional = userRepository.findById(name);
        if (userOptional.isPresent()) {
            return userOptional.get().getConversationHistory().stream()
                    .filter(entry -> !entry.getTimestamp().isBefore(startDate) && !entry.getTimestamp().isAfter(endDate))
                    .toList();
        }
        return null;
    }

    public User.ConversationEntry addConversationEntry(String name, User.ConversationEntry conversationEntry) {
        Optional<User> userOptional = userRepository.findById(name);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.addConversationEntry(conversationEntry);
            userRepository.save(user);
            return conversationEntry;
        }
        return null;
    }

    // Activity and Calorie related operations
    public User addHistoricalActivity(String name, User.HistoricalActivity activity) {
        Optional<User> userOptional = userRepository.findById(name);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.addHistoricalActivity(activity);
            return userRepository.save(user);
        }
        return null;
    }

    public User addHistoricalCalories(String name, User.HistoricalCalories calories) {
        Optional<User> userOptional = userRepository.findById(name);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.addHistoricalCalories(calories);
            return userRepository.save(user);
        }
        return null;
    }

    public User addHistoricalMeal(String name, User.HistoricalMeal meal) {
        Optional<User> userOptional = userRepository.findById(name);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.addHistoricalMeal(meal);
            return userRepository.save(user);
        }
        return null;
    }

    public List<User.ConversationEntry> getConversationHistoryById(long id) {
        return userRepository.findById(id)
                .map(User::getConversationHistory)
                .orElse(null);
    }

    public List<User.ConversationEntry> getConversationHistoryBetweenDatesById(
            long id, ZonedDateTime startDate, ZonedDateTime endDate) {
        return userRepository.findById(id)
                .map(user -> user.getConversationHistory().stream()
                        .filter(entry -> {
                            ZonedDateTime timestamp = entry.getTimestamp();
                            return !timestamp.isBefore(startDate) && !timestamp.isAfter(endDate);
                        })
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    public User.ConversationEntry addConversationEntryById(long id, User.ConversationEntry conversationEntry) {
        return userRepository.findById(id)
                .map(user -> {
                    // Set current timestamp if not provided
                    if (conversationEntry.getTimestamp() == null) {
                        conversationEntry.setTimestamp(ZonedDateTime.now());
                    }

                    user.getConversationHistory().add(conversationEntry);
                    userRepository.save(user);
                    return conversationEntry;
                })
                .orElse(null);
    }

    public User addHistoricalActivityById(long id, User.HistoricalActivity activity) {
        return userRepository.findById(id)
                .map(user -> {
                    user.getHistoricalActivities().add(activity);
                    return userRepository.save(user);
                })
                .orElse(null);
    }

    public User addHistoricalCaloriesById(long id, User.HistoricalCalories calories) {
        return userRepository.findById(id)
                .map(user -> {
                    user.getHistoricalCalories().add(calories);
                    return userRepository.save(user);
                })
                .orElse(null);
    }

    public User addHistoricalMealById(long id, User.HistoricalMeal meal) {
        return userRepository.findById(id)
                .map(user -> {
                    user.getHistoricalMeals().add(meal);
                    return userRepository.save(user);
                })
                .orElse(null);
    }
    public List<User.HistoricalMeal> getUserMealsById(long id) {
        return userRepository.findById(id)
                .map(User::getHistoricalMeals)
                .orElse(null);
    }
}