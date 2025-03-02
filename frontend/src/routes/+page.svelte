<script lang="ts">
	import fav from '$lib/icons/fav.svg';
	import type { PageData } from './$types';

	export let data: PageData;
</script>

<div class="relative h-[852px] w-[393px] overflow-hidden border border-black bg-[#f5efe7]">
	<div
		class="absolute left-[40px] top-[108px] h-[150px] w-[314px] text-center font-['Lateef'] text-5xl font-normal leading-[50px] text-black/80"
	>
		Welcome back <br />Henry
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
	<div
		class="absolute left-[101px] top-[380px] text-center font-['Inter'] text-base font-normal leading-snug text-[#1e1e1e]"
	>
		How was your run today?
	</div>
	<div
		class="absolute left-[47px] top-[739px] inline-flex h-[29px] w-[299px] items-center justify-start gap-4"
	>
		<button
			class="flex h-10 shrink grow basis-0 cursor-pointer items-center justify-center gap-2 overflow-hidden rounded-lg bg-[#f9dfc5] p-3 hover:shadow"
		>
			<div class="font-['Inter'] text-base font-normal leading-none text-[#303030]">
				Track a meal
			</div>
		</button>
		<button
			class="flex h-10 shrink grow basis-0 cursor-pointer items-center justify-center gap-2 overflow-hidden rounded-lg border border-[#2c2c2c] bg-[#2c2c2c] p-3 hover:shadow"
		>
			<div class="font-['Inter'] text-base font-normal leading-none text-neutral-100">
				Dashboard
			</div>
		</button>
	</div>
	<div
		class="absolute left-[59px] top-[467px] inline-flex h-[126px] w-[275px] flex-col items-start justify-start gap-2"
	>
		<div class="self-stretch"></div>
		<div
			class="inline-flex items-start justify-start self-stretch overflow-hidden rounded-lg border border-[#d9d9d9] bg-[#f9dfc5] px-4 py-3"
		>
			<input
				class="shrink grow basis-0 font-['Inter'] text-base font-normal leading-snug text-[#1e1e1e] outline-none"
				placeholder="Chat..."
			/>
			<div data-svg-wrapper>
				<svg width="8" height="8" viewBox="0 0 8 8" fill="none" xmlns="http://www.w3.org/2000/svg">
					<path
						d="M6.523 0.353546L0.353516 6.52303M6.98064 3.89592L3.8959 6.98066"
						stroke="#B3B3B3"
					/>
				</svg>
			</div>
		</div>
	</div>
</div>
