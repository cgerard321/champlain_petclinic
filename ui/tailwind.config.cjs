// tailwind.config.cjs

module.exports = {
	mode: 'jit',
	// add this section
	content: ['./src/**/*.html', './src/**/*.svelte'],
	darkMode: 'media', // or 'media' or 'class'
	theme: {
		extend: {}
	},
	variants: {
		extend: {}
	},
	plugins: [
		require('daisyui'),
		function ({ addVariant }) {
			addVariant('children', '& > *');
		}
	]
};
