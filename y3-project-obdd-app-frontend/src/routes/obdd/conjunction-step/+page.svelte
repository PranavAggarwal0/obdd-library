<script>
	let foo = '';
	let bar = '';
	let ord = '';
	let svg = null;
	let result = '';
	let c = -1;
	let show_info = false;

	async function doPost() {
		const res = await fetch('http://localhost:8080/conjunction/step', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				formula1: foo,
				formula2: bar,
				ordering: ord.split(' ')
			})
		});

		const data = await res.text();
		svg = JSON.parse(data);
	}

	function step() {
		if (svg) {
			if (c < svg.length - 1) {
				c = c + 1;
				result = svg[c];
			}
		}
	}

	function back() {
		if (svg) {
			if (c > 0) {
				c = c - 1;
				result = svg[c];
			}
		}
	}

	function clear() {
		foo = '';
		bar = '';
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
		This page allows you to visualise how the conjunction algorithm works step by step on a global
		DAG. Note that the algorithm is recursive, which is why nodes are first added near the bottom
		and then to the top.
	</p>
	<button type="button" on:click={toggleInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="init"><b>Conjunction, step by step</b></p>
	<br />
	<input bind:value={ord} placeholder="Enter Ordering" />
	<input bind:value={foo} placeholder="Enter Formula 1" />
	<input bind:value={bar} placeholder="Enter Formula 2" />
	{#if !svg}
		<button type="button" on:click={doPost}> Submit </button>
		<button type="button" on:click={toggleInfo}> Info </button>
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
