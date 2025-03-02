<script lang="ts">
	export let percentage: number;
	export let color: string = '#F9DFC5';
	export let size: number = 120;
	export let strokeWidth: number = 10;

	$: radius = (size - strokeWidth) / 2;
	$: circumference = radius * 2 * Math.PI;
	$: strokeDashoffset = circumference - (percentage / 100) * circumference;

	$: fontSize = size / 4;
</script>

<div class="progress-ring" style="width: {size}px; height: {size}px;">
	<svg class="progress-ring__circle" width={size} height={size}>
		<circle
			stroke="#E2E8F0"
			stroke-width={strokeWidth}
			fill="transparent"
			r={radius}
			cx={size / 2}
			cy={size / 2}
		/>
		<circle
			stroke={color}
			stroke-width={strokeWidth}
			stroke-linecap="round"
			fill="transparent"
			r={radius}
			cx={size / 2}
			cy={size / 2}
			style="stroke-dasharray: {circumference} {circumference}; stroke-dashoffset: {strokeDashoffset};"
		/>
		<text
			x="50%"
			y="50%"
			text-anchor="middle"
			dominant-baseline="central"
			font-size={fontSize}
			font-weight="bold"
			transform="rotate(90 {size / 2} {size / 2})"
		>
			{percentage}%
		</text>
	</svg>
</div>

<style>
	.progress-ring__circle {
		transition: stroke-dashoffset 0.35s;
		transform: rotate(-90deg);
		transform-origin: 50% 50%;
	}
</style>
