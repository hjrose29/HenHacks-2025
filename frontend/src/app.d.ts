// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	type User = {
		id: number,
		name: string,
		weight: number
		height: number
	}
	type Nutrient = {
		current: number;
		goal: number;
	};

	type Meal = {
		id: string;
		name: string;
		time: string;
		calories: number;
		protein: number;
		carbs: number;
		fat: number;
	};

	type Workout = {
		id: string;
		type: string;
		duration: number;
		caloriesBurned: number;
		date: string;
	};

	type DailyStats = {
		calories: Nutrient;
		protein: Nutrient;
		carbs: Nutrient;
		fat: Nutrient;
	};

	type UserData = {
		dailyStats: DailyStats;
		meals: Meal[];
		recentWorkouts: Workout[];
	};


	namespace App {
		// interface Error {}
		interface Locals {
			user?: User
		}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}

	}
}

export { };
