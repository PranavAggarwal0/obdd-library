<script>
	let foo = '';
	let ord = '';
	let svg = null;
	let result = '';
	let result1 = '';
	let c = -1;
	let loading = false;
	let show_info = false;

	async function doPost() {
		loading = true;
		const res = await fetch('http://localhost:8080/sifting/step', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				formula: foo,
				ordering: ord.split(' ')
			})
		});
		loading = false;
		const data = await res.text();
		svg = JSON.parse(data);
	}

	function step() {
		if (svg) {
			if (c < svg.length - 1) {
				c = c + 1;
				result = svg[c];
				c = c + 1;
				result1 = svg[c];
			}
		}
	}

	function back() {
		if (svg) {
			if (c > 0) {
				c = c - 1;
				result = svg[c];
				c = c - 1;
				result1 = svg[c];
			}
		}
	}

	function clear() {
		foo = '';
		ord = '';
		svg = null;
		result = '';
		c = 0;
	}

	function toggleInfo() {
		show_info = !show_info;
	}
</script>

{#if show_info}
	<p id="init">
		The sifting algorithm proceeds as follows. The variables appearing in the DAG are sorted by
		number of occurences. In this order, the variables are first moved to the bottom of the DAG, and
		then to the top by means of adjacent variable swaps. The optimal position for the variable is
		recorded in this process, and the variable is then moved to this position. Note that this
		algorithm is only a heuristic, it isn't guaranteed to provide the optimal position for each
		variable.
	</p>

	<button type="button" on:click={toggleInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="init"><b>Visualise the sifting algorithm step by step:</b></p>
	<br />
	<input bind:value={ord} placeholder="Enter Ordering" />
	<input bind:value={foo} placeholder="Enter Formula" />
	{#if !svg}
		<button type="button" on:click={doPost}> Submit </button>
		<button type="button" on:click={toggleInfo}> Info </button>
	{/if}
	{#if loading}
		<div class="spinner" id="spin">Loading...</div>
	{/if}
	{#if svg}
		<button type="button" on:click={step}> Step Forward </button>
		<button type="button" on:click={back}> Step Back </button>
		<button type="button" on:click={clear}> Clear </button>
		<button type="button" on:click={toggleInfo}> Info </button>
	{/if}

	<p><b>Result:</b></p>
	<pre id="image">
{@html result}
{@html result1}
</pre>
{/if}

<style>
	input {
		position: relative;
		top: 40%;
		left: 8%;
		width: 50%;
	}
	button {
		position: relative;
		top: 45%;
		left: 8%;
	}
	#spin {
		position: relative;
		top: 45%;
		left: 8%;
	}
	#init {
		position: relative;
		width: 800px;
		word-wrap: break-word;
	}
	p {
		position: relative;
		top: 35%;
		left: 8%;
	}
	#image {
		position: relative;
		left: 8%;
		top: 2%;
	}
</style>
