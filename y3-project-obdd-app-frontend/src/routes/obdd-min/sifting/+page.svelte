<script>
	let foo = '';
	let ord = '';
	let result = '';
	let show_info = false;

	async function doPost() {
		const res = await fetch('http://localhost:8080/sifting', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				formula: foo,
				ordering: ord.split(' ')
			})
		});

		const data = await res.text();
		result = data;
	}

	function toggleInfo() {
		show_info = !show_info;
	}
</script>

{#if show_info}
	<p id="init">
		OBDD sizes are sensitive to variable ordering. The sifting algorithm aims to reduce the size of
		the OBDD by sifting variables up and down to estimate their optimal position. This page allows
		you to enter an initial ordering and a formula for which you want to compute the OBDD. The
		result shown is the OBDD after the sifting algorithm is applied when starting with the given
		ordering. To see how the algorithm proceeds in more detail, check out the sifting step-by-step
		page.
	</p>

	<button type="button" on:click={toggleInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="init"><b>Enter a formula to compute its OBDD after sifting:</b></p>
	<br />

	<input bind:value={ord} placeholder="Enter Ordering" />
	<input bind:value={foo} placeholder="Enter Formula" />
	<button type="button" on:click={doPost}> Get OBDD </button>
	<button type="button" on:click={toggleInfo}> Info </button>
	<p><b>Result:</b></p>
	<pre id="image">
{@html result}
</pre>
{/if}

<style>
	input {
		position: relative;
		top: 40%;
		left: 16%;
		width: 50%;
	}
	button {
		position: relative;
		top: 45%;
		left: 16%;
	}
	#init {
		position: relative;
		width: 800px;
		word-wrap: break-word;
	}
	p {
		position: relative;
		top: 35%;
		left: 16%;
	}
	#image {
		position: relative;
		left: 16%;
		top: 2%;
	}
</style>
