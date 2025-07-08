<script>
	let files;
	let num = '';
	let show_info = false;

	function upload() {
		if (files) {
			for (const file of files) {
				getFileContent(file);
			}
		}
	}

	async function getFileContent(file) {
		await file.text().then((text) => {
			doPost(text);
		});
	}

	let result = '';

	async function doPost(filcon) {
		const res = await fetch('http://localhost:8080/build/qdimacs', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				formula: filcon,
				vars: num
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
		This page allows you to build an OBDD for input specified as a QDIMACS file. For more
		information about this format, check out <a href="http://www.qbflib.org/qdimacs.html"
			>this page</a
		>. Note that you will need to provide the number of variables to correctly instantiate the OBDD.
	</p>
	<button type="button" on:click={toggleInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="init"><b>Get OBDD from a file in QDIMACS format:</b></p>
	<br />
	<input bind:value={num} placeholder="Enter Number of Variables" />
	<input bind:files id="dimacs" type="file" />
	<button type="button" on:click={toggleInfo}> Info </button>
	{#if files}
		<button type="button" on:click={upload}> Get OBDD </button>
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
