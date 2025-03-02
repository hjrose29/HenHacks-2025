<script lang="ts">
	import { onMount } from 'svelte';
	import { env } from '$env/dynamic/public';
	import { PUBLIC_GEMINI_SERVICE_URL, PUBLIC_USER_SERVICE_URL } from '$env/static/public';

	// Define types based on your provided data structure
	type Exercise = {
		name: string;
		reps: number;
		sets: number;
	};

	type Workout = {
		workout_type: string;
		duration_minutes: number;
		description: string;
		exercises: Exercise[];
	};

	// State variables
	let workout: Workout | null = null;
	let isLoading = true;
	let error: string | null = null;

	// Sample data for development (replace with API call)
	const sampleWorkout: Workout = {
		workout_type: 'Mixed',
		duration_minutes: 45,
		description:
			'This workout combines cardio and strength training for a balanced fitness routine, improving both cardiovascular health and muscular strength.',
		exercises: [
			{
				name: 'Jumping Jacks',
				reps: 30,
				sets: 3
			},
			{
				name: 'Push-ups',
				reps: 10,
				sets: 3
			},
			{
				name: 'Squats',
				reps: 15,
				sets: 3
			},
			{
				name: 'Lunges (each leg)',
				reps: 12,
				sets: 3
			},
			{
				name: 'Plank',
				reps: 30,
				sets: 3
			}
		]
	};

	// Function to fetch workout from API
	async function fetchWorkout() {
		isLoading = true;
		error = null;

		try {
			console.log('getting workoutplan');

			// Change to POST method since you're sending a body
			const request = await fetch(
				`${PUBLIC_GEMINI_SERVICE_URL}/workout-plan?prompt="uniqueworkout"`,
				{
					method: 'GET', // Changed from GET to POST
					headers: { 'Content-Type': 'application/json' }
				}
			);

			// Check if the request was successful before parsing JSON
			if (!request.ok) {
				throw new Error(`Error: ${request.status}`);
			}

			// Parse JSON after checking request.ok
			let response = await request.json();

			workout = response;
			console.log(workout);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to fetch workout';
			console.error(error);

			// For development only - fallback to sample data on error
			// Remove this in production
			workout = sampleWorkout;
		} finally {
			isLoading = false;
		}
	}

	// Get icon based on exercise name
	function getExerciseIcon(exerciseName: string): string {
		// Convert exercise name to lowercase and remove spaces/special chars for matching
		const normalizedName = exerciseName.toLowerCase().replace(/\s+/g, '-').replace(/[()]/g, '');

		// Return the appropriate SVG path
		return `/icons/${normalizedName}.svg`;
	}

	// Format exercise details
	function formatExerciseDetails(exercise: Exercise): string {
		return `${exercise.sets} sets × ${exercise.reps} reps`;
	}

	onMount(() => {
		fetchWorkout();
	});
	export let data: { user: User };
</script>

<div class="min-h-screen bg-[#F5EFE7] p-4 pb-12 text-[#000000CC]">
	<header class="mb-8 pt-6">
		<h1 class="text-center text-3xl font-bold">Today's Workout</h1>
	</header>

	<main class="mx-auto max-w-3xl">
		{#if isLoading}
			<div class="flex h-64 items-center justify-center">
				<div
					class="h-12 w-12 animate-spin rounded-full border-b-2 border-t-2 border-[#F9DFC5]"
				></div>
			</div>
		{:else if error}
			<div
				class="relative rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700"
				role="alert"
			>
				<strong class="font-bold">Error!</strong>
				<span class="block sm:inline"> {error}</span>
				<button
					class="mt-4 rounded bg-[#F9DFC5] px-4 py-2 font-bold text-[#000000CC] hover:bg-[#F9DFC5]/80"
					on:click={fetchWorkout}
				>
					Try Again
				</button>
			</div>
		{:else if !workout}
			<div class="py-10 text-center">
				<p class="text-xl">No workout scheduled for today</p>
			</div>
		{:else}
			<!-- Workout Summary -->
			<div class="mb-8 overflow-hidden rounded-lg bg-white shadow-md">
				<div class="bg-[#F9DFC5] p-4">
					<div class="flex items-center justify-between">
						<h2 class="text-2xl font-bold">{workout.workout_type} Workout</h2>
						<span class="rounded-full bg-white px-3 py-1 text-sm font-medium">
							{workout.duration_minutes} min
						</span>
					</div>
				</div>
				<div class="p-4">
					<p class="text-[#000000CC]">{workout.description}</p>
				</div>
			</div>

			<!-- Exercises List -->
			<h3 class="mb-4 text-xl font-semibold">Exercises</h3>
			<div class="space-y-4">
				{#each workout.exercises as exercise, i}
					<div
						class="overflow-hidden rounded-lg bg-white shadow-md transition-shadow duration-300 hover:shadow-lg"
					>
						<div class="flex items-center p-4">
							<div class="mr-4 flex-shrink-0 rounded-full bg-[#F9DFC5] p-3">
								<img src="/icons/dumbbell.svg" alt={exercise.name} class="h-8 w-8" />
							</div>
							<div class="flex-grow">
								<h4 class="text-lg font-medium">{exercise.name}</h4>
								<p class="text-[#000000CC]/80">{formatExerciseDetails(exercise)}</p>
							</div>
							<div
								class="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-[#F9DFC5]/30"
							>
								{i + 1}
							</div>
						</div>
					</div>
				{/each}
			</div>

			<button
				class="mt-8 w-full rounded-lg bg-[#F9DFC5] px-4 py-3 font-bold text-[#000000CC] shadow-md hover:bg-[#F9DFC5]/80"
				on:click={fetchWorkout}
			>
				Refresh Workout
			</button>
		{/if}
	</main>
</div>

<style>
	:global(body) {
		background-color: #f5efe7;
		font-family:
			system-ui,
			-apple-system,
			BlinkMacSystemFont,
			'Segoe UI',
			Roboto,
			sans-serif;
	}
</style>
