<script lang="ts">
	export let navItems: NavItem[] = [];
</script>

<nav class="navbar mb-2 shadow-lg bg-neutral text-neutral-content rounded-box sm:rounded-none">
	<span class="text-3xl text-primary-focus navbar-start">PetClinic</span>
	<ul class="flex navbar-end">
		{#each navItems as item}
			<li>
				<a href={item.href} class="text-base text-primary-focus">{item.text}</a>
			</li>
		{/each}
	</ul>
</nav>
