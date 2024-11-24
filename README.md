<h1 align=center>the Wikiverse</h1>
<ul align=center>
  <h2>
    <a href=https://github.com/horaciovelvetine/wikidata-universe-client>Frontend</a> |
    <a href=https://github.com/horaciovelvetine/wikidata-universe-api>Backend</a> |
    <a href=https://github.com/horaciovelvetine/horaciovelvetine/blob/main/assets/docs/THE_WIKIVERSE_OVERVIEW.md>Documentation & Resources </a>
  </h2>
</ul>

<h2 align=center>The Wikiverse is a web app (and API) that allows you to search Wikidata and explore topics in 3D space. Leveraging Wikidata's publically available tools and api for data, the Wikiverse dynamically generates a graph of the result and it's related topics. This project is currently in Alpha and serves exclusively as an educational project started by the question 'What would wikipedia look like in 3D space?'</h2>

<img align=center src=.github/readme_assets/wikipedia_in3D_v0.0.1.png>
<p align=center>This is what Wikipedia looks like in 3D space, just to get that answer out of the way</p>  

<h4>Special attention has been taken throughout the development process to ensure any learning opportunity encountered was well documented. The <a href=https://github.com/horaciovelvetine/horaciovelvetine/blob/main/assets/docs/THE_WIKIVERSE_OVERVIEW.md>Documentation & Resources</a> section includes links to additional write-ups, copies of reference material, links to code, sample & pseudo code, and a record of the complete prototyping process from question to deploy</h4>

## `main` Azure Deployment

<h4></h4>

- **Java Version 21** per Azure - the build will fail on the back of that.

## `staging` Integration & Prep

<h4></h4>

## `v0.0.1` (Current) Alpha

<h3>API to fetch and filter Wikidata into a Graph (Vertices & Edges) format, which can then be given a layout and served as .json for the frontend. Handles a Err's through all implemented services by responding with appropriate status and Err details in body of response. Minimal filter-ing established to omit irrelevant data and bad results.</h3>

- **Stateless Data:** All requests are posted with the needed data, or only use search params to assemble the correct response data. No state is stored for the client or by the API for any request.
- **Creates a 3D Layout:** Positions Vertices (Item Documents) in a scaled 3D space based on Edges created from Statements to cluster things based on their relationship to one another.
- **Tutorial:** Serves and builds data in coordination with client for effective an introduction to the Wikiverse.    



