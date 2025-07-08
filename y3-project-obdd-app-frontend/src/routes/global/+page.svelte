<script>
	let build_formula = '';
	let id_1 = '';
	let id_2 = '';
	let neg_id = '';
	let show_info = false;

	let result = '';

	let build_vis = true;
	let con_vis = false;
	let dis_vis = false;
	let neg_vis = false;

	function toggleBuildVis() {
		build_vis = true;
		con_vis = false;
		dis_vis = false;
		neg_vis = false;
		sift_vis = false;
	}

	function toggleConVis() {
		build_vis = false;
		con_vis = true;
		dis_vis = false;
		neg_vis = false;
		sift_vis = false;
	}

	function toggleDisVis() {
		build_vis = false;
		con_vis = false;
		dis_vis = true;
		neg_vis = false;
		sift_vis = false;
	}

	function toggleNegVis() {
		build_vis = false;
		con_vis = false;
		dis_vis = false;
		neg_vis = true;
		sift_vis = false;
	}

	function toggleShowInfo() {
		show_info = !show_info;
	}

	async function doBuild() {
		const res = await fetch('http://localhost:8080/global/build', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				formula: build_formula
			})
		});
		const data = await res.text();
		result = data;
	}

	async function doCon() {
		const res = await fetch('http://localhost:8080/global/conjunction', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				id1: id_1,
				id2: id_2
			})
		});
		const data = await res.text();
		result = data;
	}

	async function doDis() {
		const res = await fetch('http://localhost:8080/global/disjunction', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				id1: id_1,
				id2: id_2
			})
		});
		const data = await res.text();
		result = data;
	}

	async function doNeg() {
		const res = await fetch('http://localhost:8080/global/negation', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				id: neg_id
			})
		});
		const data = await res.text();
		result = data;
	}
</script>

{#if show_info}
	<p id="init">
		This page allows you to explore how OBDDs can be used to represent several formulas by means of
		a multi-rooted DAG. This has the benefit that sub-DAGs are still shared when possible, leading
		to a more compact representation. <br /><br />Different nodes in the global DAG can be
		combined/negated to yield more nodes. To add a node, enter the formula using the connectives &&,
		||, ! and ->. Note that you will have to use variables x1, x2, x3 and so on (with the ordering
		being x1,x2,x3...). To combine or negate nodes, enter their ids (shown in parentheses) in the
		relevant page.
	</p>
	<br />
	<button type="button" on:click={toggleShowInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="init"><b>Manipulate Global DAG</b></p>
	<button on:click={toggleBuildVis}> Add Node for formula </button>
	<button on:click={toggleConVis}> Conjunction </button>
	<button on:click={toggleDisVis}> Disjunction </button>
	<button on:click={toggleNegVis}> Negation </button>

	{#if build_vis}
		<p id="init">Add node for formula:</p>
		<br />
		<input bind:value={build_formula} placeholder="Enter Formula" />
		<button type="button" on:click={doBuild}> Enter </button>
		<button on:click={toggleShowInfo}> Info </button>
	{/if}

	{#if con_vis}
		<p id="init">Enter two IDs for conjunction:</p>
		<br />
		<input bind:value={id_1} placeholder="Enter ID 1" />
		<input bind:value={id_2} placeholder="Enter ID 2" />
		<button type="button" on:click={doCon}> Enter </button>
		<button on:click={toggleShowInfo}> Info </button>
	{/if}

	{#if dis_vis}
		<p id="init">Enter two IDs for disjunction:</p>
		<br />
		<input bind:value={id_1} placeholder="Enter ID 1" />
		<input bind:value={id_2} placeholder="Enter ID 2" />
		<button type="button" on:click={doDis}> Enter </button>
		<button on:click={toggleShowInfo}> Info </button>
	{/if}

	{#if neg_vis}
		<p id="init">Enter ID for negation:</p>
		<br />
		<input bind:value={neg_id} placeholder="Enter ID" />
		<button type="button" on:click={doNeg}> Enter </button>
		<button on:click={toggleShowInfo}> Info </button>
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
