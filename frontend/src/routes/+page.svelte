<script lang="ts">
	import { onMount } from 'svelte';
	import Logo from '$lib/Components/Logo.svelte';

	// User data (in a real app, this would come from your auth/user service)
	let userName = 'Alex';
	let timeOfDay = '';
	let chatMessage = '';
	let chatHistory: { sender: 'user' | 'ai'; message: string }[] = [];
	let chatInputRef: HTMLInputElement;

	// Get time of day for greeting
	onMount(() => {
		const hour = new Date().getHours();
		if (hour < 12) timeOfDay = 'morning';
		else if (hour < 18) timeOfDay = 'afternoon';
		else timeOfDay = 'evening';
	});

	// Handle chat submission
	function handleChatSubmit() {
		if (!chatMessage.trim()) return;

		// Add user message to chat
		chatHistory = [...chatHistory, { sender: 'user', message: chatMessage }];

		// Simulate AI response (in a real app, this would call your Gemini AI integration)
		setTimeout(() => {
			const responses = [
				"I see you're making good progress on your fitness goals!",
				'Have you logged your water intake today?',
				'Based on your recent activity, you might want to focus on protein intake.',
				'Your workout consistency has been great this week!'
			];
			const randomResponse = responses[Math.floor(Math.random() * responses.length)];
			chatHistory = [...chatHistory, { sender: 'ai', message: randomResponse }];
		}, 1000);

		// Clear input
		chatMessage = '';
		chatInputRef.focus();
	}
</script>

<main class="flex min-h-screen flex-col bg-[#F5EFE7] p-4 text-[#000000CC]">
	<Logo></Logo>
	<!-- Greeting Header -->
	<header class="mb-6 mt-8 text-center">
		<h1 class="text-2xl font-bold">Good {timeOfDay}, {userName}!</h1>
		<p class="mt-1 text-sm">Track your wellness journey with AI assistance</p>
	</header>

	<!-- Chat with Gemini AI -->
	<section class="mb-6 flex max-h-[50vh] flex-grow flex-col">
		<div class="rounded-t-lg border-b border-[#F9DFC5] bg-white p-3">
			<h2 class="flex items-center gap-2 font-medium">
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
					class="text-[#F9DFC5]"
				>
					<circle cx="12" cy="12" r="10" />
					<path d="M12 16v-4" />
					<path d="M12 8h.01" />
				</svg>
				Gemini Assistant
			</h2>
		</div>

		<div class="mb-2 flex-grow overflow-y-auto rounded-b-lg bg-white p-3 shadow-sm">
			{#if chatHistory.length === 0}
				<p class="my-4 text-center text-sm text-gray-400">Ask Gemini about your wellness goals</p>
			{:else}
				<div class="space-y-3">
					{#each chatHistory as chat}
						<div class={`flex ${chat.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
							<div
								class={`max-w-[80%] rounded-lg p-2 ${
									chat.sender === 'user'
										? 'rounded-tr-none bg-[#F9DFC5] text-[#000000CC]'
										: 'rounded-tl-none bg-gray-100 text-[#000000CC]'
								}`}
							>
								{chat.message}
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>

		<div class="flex gap-2">
			<input
				type="text"
				bind:value={chatMessage}
				bind:this={chatInputRef}
				placeholder="Ask Gemini about your wellness..."
				class="flex-grow rounded-lg border border-[#F9DFC5] p-3 focus:outline-none focus:ring-2 focus:ring-[#F9DFC5]"
				on:keydown={(e) => e.key === 'Enter' && handleChatSubmit()}
			/>
			<button
				on:click={handleChatSubmit}
				class="rounded-lg bg-[#F9DFC5] p-3 text-[#000000CC] transition-opacity hover:opacity-90"
			>
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
					<path d="m22 2-7 20-4-9-9-4Z" />
					<path d="M22 2 11 13" />
				</svg>
			</button>
		</div>
	</section>

	<!-- Action Buttons -->
	<section class="mb-8 grid grid-cols-2 gap-4">
		<button
			class="flex flex-col items-center justify-center rounded-lg bg-white p-4 text-[#000000CC] shadow-sm transition-shadow hover:shadow-md"
		>
			<svg
				xmlns="http://www.w3.org/2000/svg"
				width="24"
				height="24"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2"
				stroke-linecap="round"
				stroke-linejoin="round"
				class="mb-2 text-[#F9DFC5]"
			>
				<path d="M3 3v18h18" />
				<path d="m19 9-5 5-4-4-3 3" />
			</svg>
			<span class="font-medium">Your Breakdown</span>
			<span class="mt-1 text-xs">View progress & stats</span>
		</button>

		<button
			class="flex flex-col items-center justify-center rounded-lg bg-white p-4 text-[#000000CC] shadow-sm transition-shadow hover:shadow-md"
		>
			<svg
				xmlns="http://www.w3.org/2000/svg"
				width="24"
				height="24"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2"
				stroke-linecap="round"
				stroke-linejoin="round"
				class="mb-2 text-[#F9DFC5]"
			>
				<path d="M11 12H3" />
				<path d="M16 6H3" />
				<path d="M16 18H3" />
				<path d="M18 9v6" />
				<path d="M21 12h-6" />
			</svg>
			<span class="font-medium">Add Food</span>
			<span class="mt-1 text-xs">Log your meals</span>
		</button>
	</section>
</main>

<style>
	/* Custom scrollbar for chat history */
	:global(*::-webkit-scrollbar) {
		width: 6px;
	}

	:global(*::-webkit-scrollbar-track) {
		background: transparent;
	}

	:global(*::-webkit-scrollbar-thumb) {
		background-color: #f9dfc5;
		border-radius: 20px;
	}
</style>
