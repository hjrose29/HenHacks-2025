package us.salus.userservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import us.salus.userservice.models.User;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findById(long id);

    Optional<User> findByName(String name);

    // Query to find users who have logged activities in a date range
    @Query("{'historicalActivities.startDate': {$gte: ?0, $lte: ?1}}")
    List<User> findUsersWithActivitiesBetweenDates(ZonedDateTime startDate, ZonedDateTime endDate);

    // Query to find users who have not logged any meals
    @Query("{'historicalMeals': {$size: 0}}")
    List<User> findUsersWithNoMeals();

    // Find users who have burned more than a specified number of calories
    @Query("{'historicalCalories.caloriesBurned': {$gte: ?0}}")
    List<User> findUsersByMinCaloriesBurned(float minCaloriesBurned);

    // Find users who have consumed less than a specified number of calories
    @Query("{'historicalCalories.caloriesConsumed': {$lte: ?0}}")
    List<User> findUsersByMaxCaloriesConsumed(float maxCaloriesConsumed);
}
