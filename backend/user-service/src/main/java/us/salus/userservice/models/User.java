package us.salus.userservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String name;
    private long id;
    private int age;
    private float weight;
    private float height;
    private float bmr;
    private String fitnessGoal;
    private List<HistoricalCalories> historicalCalories = new ArrayList<>();
    private List<HistoricalActivity> historicalActivities = new ArrayList<>();
    private List<HistoricalMeal> historicalMeals = new ArrayList<>();
    private List<ConversationEntry> conversationHistory = new ArrayList<>();


    public User(long id, String name, int age, float weight, float height, String fitnessGoal) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.fitnessGoal = fitnessGoal;
        this.calculateBMR();
    }

    private void calculateBMR() {
        this.bmr = (10 * this.weight) + (6.25f * this.height) - (5 * this.age) + 5;
    }

    public void setWeight(float weight) {
        this.weight = weight;
        this.calculateBMR();
    }

    public void setHeight(float height) {
        this.height = height;
        this.calculateBMR();
    }

    public void setAge(int age) {
        this.age = age;
        this.calculateBMR();
    }

    public void addHistoricalCalories(HistoricalCalories caloriesEntry) {
        this.historicalCalories.add(caloriesEntry);
    }

    public void addHistoricalActivity(HistoricalActivity activityEntry) {
        this.historicalActivities.add(activityEntry);
    }

    public void addHistoricalMeal(HistoricalMeal mealEntry) {
        this.historicalMeals.add(mealEntry);
    }

    public void addConversationEntry(ConversationEntry conversationEntry) {
        this.conversationHistory.add(conversationEntry);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalCalories {
        private ZonedDateTime date;
        private float caloriesBurned;
        private float caloriesConsumed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalActivity {
        private String name;
        private String type;
        private String sportType;
        private ZonedDateTime startDate;
        private ZonedDateTime startDateLocal;
        private String timezone;
        private int utcOffset;
        private float kilojoules;
        private float averageHeartrate;
        private float maxHeartrate;
        private float sufferScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalMeal {
        private String name;
        private ZonedDateTime timestamp;
        private Macronutrients macronutrients = new Macronutrients();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Macronutrients {
            private float carbs;
            private float protein;
            private float fats;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationEntry {
        private ZonedDateTime timestamp;
        private String message;
        private String response;
    }
}
