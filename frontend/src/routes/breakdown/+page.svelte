<script lang="ts">
	import { onMount } from 'svelte';
	import ProgressRing from '$lib/Components/ProgressRing.svelte';

	// Types for our data
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

	// Mock data (replace with actual data fetching in a real app)
	let userData: UserData = {
		dailyStats: {
			calories: { current: 1500, goal: 2000 },
			protein: { current: 75, goal: 100 },
			carbs: { current: 150, goal: 250 },
			fat: { current: 50, goal: 65 }
		},
		meals: [
			{ id: '1', name: 'Breakfast', time: '08:00', calories: 400, protein: 20, carbs: 50, fat: 15 },
			{ id: '2', name: 'Lunch', time: '13:00', calories: 600, protein: 30, carbs: 70, fat: 20 },
			{ id: '3', name: 'Dinner', time: '19:00', calories: 500, protein: 25, carbs: 30, fat: 15 }
		],
		recentWorkouts: [
			{ id: '1', type: 'Running', duration: 30, caloriesBurned: 300, date: '2025-03-01' },
			{ id: '2', type: 'Weight Training', duration: 45, caloriesBurned: 200, date: '2025-02-28' }
		]
	};

	// State for expanded meal details
	let expandedMealId: string | null = null;

	// Function to toggle meal details
	function toggleMealDetails(mealId: string) {
		expandedMealId = expandedMealId === mealId ? null : mealId;
	}

	// Function to calculate percentage
	function calculatePercentage(current: number, goal: number): number {
		return Math.min(Math.round((current / goal) * 100), 100);
	}

	// Format date
	function formatDate(dateString: string): string {
		return new Date(dateString).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
	}

	// Color mapping for nutrients
	const nutrientColors = {
		calories: '#FF9500',
		protein: '#FF3B30',
		carbs: '#5856D6',
		fat: '#FF2D55'
	};
</script>

<main class="flex min-h-screen flex-col bg-[#F5EFE7] p-4 text-[#000000CC]">
	<header class="mb-6 mt-8 text-center">
		<h1 class="text-2xl font-bold">Your Dashboard</h1>
		<p class="mt-1 text-sm">Track your progress and stay motivated</p>
	</header>

	<!-- Daily Progress -->
	<section class="mb-6 rounded-lg bg-white p-4 shadow-sm">
		<h2 class="mb-3 text-lg font-semibold">Daily Progress</h2>
		<div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
			{#each Object.entries(userData.dailyStats) as [nutrient, { current, goal }]}
				<div class="flex flex-col items-center">
					<ProgressRing
						percentage={calculatePercentage(current, goal)}
						color={nutrientColors[nutrient]}
						size={100}
						strokeWidth={8}
					/>
					<p class="mt-2 text-sm font-medium capitalize">{nutrient}</p>
					<p class="text-xs text-gray-600">
						{current} / {goal}
						{nutrient === 'calories' ? 'kcal' : 'g'}
					</p>
				</div>
			{/each}
		</div>
	</section>

	<!-- Meals -->
	<section class="mb-6 rounded-lg bg-white p-4 shadow-sm">
		<h2 class="mb-3 text-lg font-semibold">Today's Meals</h2>
		<div class="space-y-3">
			{#each userData.meals as meal}
				<div class="rounded-lg border border-[#F9DFC5] p-3">
					<div class="flex items-center justify-between">
						<div>
							<h3 class="font-medium">{meal.name}</h3>
							<p class="text-sm text-gray-600">{meal.time} - {meal.calories} kcal</p>
						</div>
						<button
							on:click={() => toggleMealDetails(meal.id)}
							class="text-[#F9DFC5] transition-colors hover:text-[#000000CC]"
						>
							{expandedMealId === meal.id ? '▲' : '▼'}
						</button>
					</div>
					{#if expandedMealId === meal.id}
						<div class="mt-2 border-t border-[#F9DFC5] pt-2 text-sm">
							<p>Protein: {meal.protein}g</p>
							<p>Carbs: {meal.carbs}g</p>
							<p>Fat: {meal.fat}g</p>
						</div>
					{/if}
				</div>
			{/each}
		</div>
	</section>

	<!-- Recent Workouts -->
	<section class="mb-6 rounded-lg bg-white p-4 shadow-sm">
		<h2 class="mb-3 text-lg font-semibold">Recent Workouts</h2>
		<div class="space-y-3">
			{#each userData.recentWorkouts as workout}
				<div
					class="flex items-center justify-between border-b border-[#F9DFC5] pb-2 last:border-b-0"
				>
					<div>
						<h3 class="font-medium">{workout.type}</h3>
						<p class="text-sm text-gray-600">
							{formatDate(workout.date)} - {workout.duration} mins
						</p>
					</div>
					<div class="text-right">
						<p class="font-medium">{workout.caloriesBurned} kcal</p>
						<p class="text-sm text-gray-600">burned</p>
					</div>
				</div>
			{/each}
		</div>
	</section>

	<!-- Navigation -->
	<nav class="flex justify-between">
		<a href="/" class="text-sm text-[#000000CC] hover:underline"> ← Back to Home </a>
		<a href="/add-food" class="text-sm text-[#000000CC] hover:underline"> Add Food → </a>
	</nav>
</main>
