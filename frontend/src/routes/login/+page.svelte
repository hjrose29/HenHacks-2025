<script lang="ts">
	import fav from '$lib/icons/fav.svg';
	import strava from '$lib/icons/strava.svg';

	function login() {
		console.log('Attempting to login');
	}
</script>

<div class="relative h-[852px] w-[393px] overflow-hidden border border-black bg-[#f5efe7]">
	<div
		class="absolute left-[40px] top-[215px] h-[150px] w-[314px] text-center font-['Lateef'] text-5xl font-normal leading-[50px] text-black/80"
	>
		Welcome<br />Please link your Strava
	</div>
	<div class="absolute left-0 top-0 h-[76px] w-[393px] bg-[#f9dfc4]"></div>
	<div data-svg-wrapper class="absolute left-[111px] top-[11px]">
		<img src={fav} alt="" />
	</div>
	<div
		class="absolute left-[174px] top-[16px] h-[45px] w-[109px] font-['Italiana'] text-[40px] font-normal text-[#4a3316]"
	>
		SALUS
	</div>

	<button
		on:click={login}
		class="absolute left-[114px] top-[504px] inline-flex h-[84px] w-[166px] items-center justify-start gap-4"
	>
		<div
			class="flex h-10 shrink grow basis-0 items-center justify-center gap-2 overflow-hidden rounded-lg bg-[#f9dfc5] p-3"
		>
			<div class="font-['Inter'] text-base font-normal leading-none text-[#303030]">Login</div>
		</div>
	</button>
	<img
		on:click={login}
		class="absolute left-[122px] top-[531px] h-[29px] w-[29px] rounded"
		src={strava}
	/>
</div>
