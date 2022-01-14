<script lang="ts">
	let email: string = '';
	let password: string = '';
</script>

<div class="flex flex-col h-full justify-center">
	<div class="flex justify-center">
		<div class="w-1/6">
			<div
				class="flex flex-col break-words bg-base-200 border-base-200 border-2 rounded shadow-md md:h-[36vh]"
			>
				<div class="font-semibold bg-base-300 text-primary-focus py-3 px-6 mb-0">Login</div>

				<div class="p-6 flex-grow flex flex-col justify-evenly">
					<div class="form-control">
						<label class="label-form" for="email">
							<span>Email</span>
							<input
								placeholder="email"
								class="input-form"
								type="text"
								name="email"
								bind:value={email}
							/>
						</label>
					</div>

					<div class="form-control mt-6">
						<label class="label-form" for="password">
							<span>Password</span>
							<input
								placeholder="password"
								class="input-form"
								type="password"
								name="password"
								bind:value={password}
							/>
						</label>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<style lang="postcss">
	.label-form {
		@apply input-group input-group-vertical input-group-md text-secondary-focus;
	}

	.input-form {
		@apply input input-bordered input;
	}
</style>
