<script lang="ts">
	import { redirect } from '@sveltejs/kit';
	import { onMount } from 'svelte';

	// Food data
	let searchQuery = '';
	let foodName = '';
	let protein = '';
	let grains = '';
	let fats = '';
	let isSearching = false;
	let searchError = '';
	let searchSuccess = false;

	// Form validation
	let isSubmitting = false;
	let formError = '';
	let formSuccess = false;

	// Search for food from the API
	async function searchFood() {
		if (!searchQuery.trim()) return;

		isSearching = true;
		searchError = '';
		searchSuccess = false;

		try {
			const response = await fetch(
				`http://localhost:42070/search?q=${encodeURIComponent(searchQuery)}`
			);

			if (!response.ok) {
				throw new Error(`Search failed with status: ${response.status}`);
			}

			const data = await response.json();

			// Populate the form with the returned data
			foodName = data.name || '';
			protein = data.protein?.toString() || '';
			grains = data.grains?.toString() || '';
			fats = data.fats?.toString() || '';

			searchSuccess = true;

			// Clear search success message after 3 seconds
			setTimeout(() => {
				searchSuccess = false;
			}, 3000);
		} catch (error) {
			console.error('Error searching for food:', error);
			searchError = 'Failed to find food. Please try another search term.';
		} finally {
			isSearching = false;
		}
	}

	// Submit the food entry
	function submitFood() {
		// Basic validation
		if (!foodName.trim()) {
			formError = 'Food name is required';
			return;
		}

		if (!protein && !grains && !fats) {
			formError = 'At least one nutritional value is required';
			return;
		}

		isSubmitting = true;
		formError = '';

		const foodData = {
			name: foodName,
			protein: protein ? parseFloat(protein) : 0,
			grains: grains ? parseFloat(grains) : 0,
			fats: fats ? parseFloat(fats) : 0,
			timestamp: new Date().toISOString()
		};

		console.log('Submitting food data:', foodData);

		setTimeout(() => {
			isSubmitting = false;
			formSuccess = true;

			// Reset form after successful submission
			setTimeout(() => {
				foodName = '';
				protein = '';
				grains = '';
				fats = '';
				formSuccess = false;
			}, 2000);
		}, 1000);
	}

	// Handle Enter key in search input
	function handleSearchKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			searchFood();
		}
	}
</script>

<main class="flex min-h-screen flex-col bg-[#F5EFE7] p-4 text-[#000000CC]">
	<!-- Header -->
	<header class="mb-6 mt-8 text-center">
		<h1 class="text-2xl font-bold">Add Food</h1>
		<p class="mt-1 text-sm">Track your nutrition intake</p>
	</header>

	<!-- Search Bar -->
	<section class="mb-6">
		<div class="relative">
			<input
				type="text"
				bind:value={searchQuery}
				placeholder="Search for a food..."
				class="w-full rounded-lg border border-[#F9DFC5] p-3 pr-12 focus:outline-none focus:ring-2 focus:ring-[#F9DFC5]"
				on:keydown={handleSearchKeydown}
			/>
			<button
				on:click={searchFood}
				class="absolute right-2 top-1/2 -translate-y-1/2 transform rounded-lg bg-[#F9DFC5] p-2 text-[#000000CC] transition-opacity hover:opacity-80"
				disabled={isSearching}
			>
				{#if isSearching}
					<svg
						class="h-5 w-5 animate-spin"
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
					>
						<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
						></circle>
						<path
							class="opacity-75"
							fill="currentColor"
							d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
						></path>
					</svg>
				{:else}
					<svg
						xmlns="http://www.w3.org/2000/svg"
						width="20"
						height="20"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2"
						stroke-linecap="round"
						stroke-linejoin="round"
					>
						<circle cx="11" cy="11" r="8"></circle>
						<path d="m21 21-4.3-4.3"></path>
					</svg>
				{/if}
			</button>
		</div>

		{#if searchError}
			<p class="mt-2 text-sm text-red-500">{searchError}</p>
		{/if}

		{#if searchSuccess}
			<p class="mt-2 text-sm text-green-600">Food found! Details populated below.</p>
		{/if}
	</section>

	<!-- Food Form -->
	<form on:submit|preventDefault={submitFood} class="mb-6 rounded-lg bg-white p-4 shadow-sm">
		<div class="space-y-4">
			<!-- Food Name -->
			<div>
				<label for="foodName" class="mb-1 block text-sm font-medium">Food Name</label>
				<input
					id="foodName"
					type="text"
					bind:value={foodName}
					placeholder="e.g., Grilled Chicken Breast"
					class="w-full rounded-lg border border-[#F9DFC5] p-3 focus:outline-none focus:ring-2 focus:ring-[#F9DFC5]"
					required
				/>
			</div>

			<!-- Nutritional Values -->
			<div class="grid grid-cols-3 gap-3">
				<!-- Protein -->
				<div>
					<label for="protein" class="mb-1 block text-sm font-medium">Protein (g)</label>
					<input
						id="protein"
						type="number"
						bind:value={protein}
						min="0"
						step="0.1"
						placeholder="0"
						class="w-full rounded-lg border border-[#F9DFC5] p-3 focus:outline-none focus:ring-2 focus:ring-[#F9DFC5]"
					/>
				</div>

				<!-- Grains -->
				<div>
					<label for="grains" class="mb-1 block text-sm font-medium">Grains (g)</label>
					<input
						id="grains"
						type="number"
						bind:value={grains}
						min="0"
						step="0.1"
						placeholder="0"
						class="w-full rounded-lg border border-[#F9DFC5] p-3 focus:outline-none focus:ring-2 focus:ring-[#F9DFC5]"
					/>
				</div>

				<!-- Fats -->
				<div>
					<label for="fats" class="mb-1 block text-sm font-medium">Fats (g)</label>
					<input
						id="fats"
						type="number"
						bind:value={fats}
						min="0"
						step="0.1"
						placeholder="0"
						class="w-full rounded-lg border border-[#F9DFC5] p-3 focus:outline-none focus:ring-2 focus:ring-[#F9DFC5]"
					/>
				</div>
			</div>

			{#if formError}
				<p class="text-sm text-red-500">{formError}</p>
			{/if}

			{#if formSuccess}
				<div class="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-green-700">
					Food added successfully!
				</div>
			{/if}

			<!-- Submit Button -->
			<button
				type="submit"
				class="flex w-full items-center justify-center rounded-lg bg-[#F9DFC5] p-3 font-medium text-[#000000CC] transition-opacity hover:opacity-90"
				disabled={isSubmitting}
			>
				{#if isSubmitting}
					<svg
						class="mr-2 h-5 w-5 animate-spin"
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
					>
						<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
						></circle>
						<path
							class="opacity-75"
							fill="currentColor"
							d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
						></path>
					</svg>
					Adding Food...
				{:else}
					Add Food to Tracker
				{/if}
			</button>
		</div>
	</form>

	<!-- Back Button -->
	<a href="/" class="text-center text-sm text-[#000000CC] hover:underline"> ← Back to Dashboard </a>
</main>
