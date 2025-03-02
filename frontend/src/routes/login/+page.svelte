<script lang="ts">
	import Logo from '$lib/Components/Logo.svelte';
	import { env } from '$env/dynamic/public';
	import { PUBLIC_USER_SERVICE_URL } from '$env/static/public';

	function login() {
		console.log('Attempting to login');
	}
</script>

<div class="flex min-h-screen w-full items-center justify-center bg-[#F5EFE7] p-4">
	<div class="w-full max-w-md">
		<div class="space-y-8 rounded-xl bg-white p-8 shadow-lg">
			<!-- Logo and Header -->
			<div class="space-y-2 text-center">
				<Logo />
				<h1 class="text-2xl font-bold text-[#2d1e5f]">Welcome</h1>
				<p class="text-gray-500">Sign in to continue to your dashboard</p>
			</div>

			<!-- Strava Login Button -->
			<a
				href="{PUBLIC_USER_SERVICE_URL}/auth/login"
				on:click={login}
				class="flex w-full items-center justify-center gap-3 rounded-lg bg-[#FC4C02] px-4 py-3 font-medium text-white transition-colors duration-200 hover:bg-[#e64500]"
			>
				<!-- Strava Logo -->
				<img src="icons/strava.svg" />
				Connect with Strava
			</a>

			<!-- Additional Info -->
		</div>

		<!-- Footer -->
		<div class="mt-8 text-center text-sm text-gray-500">
			<p>
				Need help? <a href="#" class="text-[#7e5bf5] hover:underline"
					>We don't have support Support</a
				>
			</p>
		</div>
	</div>
</div>

<style>
	/* Additional styles can be added here if needed */
</style>
