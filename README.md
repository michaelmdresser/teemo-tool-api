# teemo-tool-api

This is an API server that provides access to data collected by [Teemo Tool](https://github.com/michaelmdresser/teemo-tool). Exposed in website form by the [Teemo Tool Site](https://github.com/michaelmdresser/teemo-tool-site).

## Usage

`lein run` to run the server. Database location is currently hardcoded.

## Endpoints

#### *GET* `/bets/blue`

Returns JSON in the form `{"bets": [400, 300, 10000, ...]}`. Gets the timestamp of the most recent bet recorded and then aggregates all bets made on blue team made after 5 minutes before the most recent bet. Returns a list of integers representing those bets' amounts.

#### *GET* `/bets/red`

Like `/bets/blue` but for red team.

#### *GET* `/test/bets/generate`

Returns immediately. Begins a job that empties the bets recorded in the test table and then slowly puts 10-50 (random) bets into the test bets table. Intended to help frontend developers see behavior on bets as they come in without running their own API server.

#### *GET* `/test/bets/blue`

Like `/bets/blue` but accesses data in the test table, which can only be modified using the `/test/bets/generate` endpoint. Intended for testing frontend behavior.

#### *GET* `/test/bets/red`

Like `/test/bets/blue` but for red team.

#### *GET* `/bets/rothfuss/latest`

Returns JSON of the form `{"team": "blue", "amount": 15000, "timestamp": "ISO 8601 Here"}`. Contains the bet information for Patrick Rothfuss' latest bet on the Salty Teemo stream.

#### *GET* `/bets/rothfuss/all`

Returns a JSON array of objects that match the response from `/bets/rothfuss/latest` that encompasses all bets made by Patrick Rothfuss that were recorded by the bot.

## Future Work

- Config option for database path

### Bugs

- Fine tune the window to gather bets from. Instant remakes have some overlap with the start of the betting window on the next match, causing a mis-reporting of data.
